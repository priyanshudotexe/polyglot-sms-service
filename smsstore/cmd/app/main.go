package main

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"os/signal"
	"smsstore/internal/config"
	"smsstore/internal/repository"
	"smsstore/internal/routes"
	"smsstore/pkg/models"
	"syscall"
	"time"

	"github.com/segmentio/kafka-go"
)

func main() {
	repository.GetClient()

	cfg := config.LoadConfig()

	router := routes.SetupRoutes()

	server := &http.Server{
		Addr:         cfg.ServerPort,
		Handler:      router,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		log.Printf("HTTP server listening on %s", cfg.ServerPort)
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Could not listen on %s: %v\n", cfg.ServerPort, err)
		}
	}()

	// Start Kafka consumer in goroutine
	go consumeKafkaMessages(cfg)

	// Setup graceful shutdown
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		log.Fatal("Server forced to shutdown:", err)
	}

	if err := repository.DisconnectMongo(); err != nil {
		log.Println("Error disconnecting MongoDB:", err)
	}

	log.Println("Server exited")
}

func consumeKafkaMessages(cfg *config.Config) {
	reader := kafka.NewReader(kafka.ReaderConfig{
		Brokers:  cfg.KafkaBrokers,
		Topic:    cfg.KafkaTopic,
		GroupID:  cfg.KafkaGroupID,
		MinBytes: 10e3, // 10KB
		MaxBytes: 10e6, // 10MB
	})
	defer reader.Close()

	log.Printf("Kafka consumer started - Topic: %s, GroupID: %s", cfg.KafkaTopic, cfg.KafkaGroupID)

	for {
		msg, err := reader.ReadMessage(context.Background())
		if err != nil {
			log.Printf("Error reading Kafka message: %v", err)
			continue
		}

		var smsEvent models.SmsEvent
		if err := json.Unmarshal(msg.Value, &smsEvent); err != nil {
			log.Printf("Error unmarshaling SMS event: %v", err)
			continue
		}

		log.Printf("Received SMS event for: %s (status: %s)", smsEvent.PhoneNumber, smsEvent.Status)

		// Store message in MongoDB with status
		if err := repository.AddMessageToUser(smsEvent.PhoneNumber, smsEvent.Message, smsEvent.Status); err != nil {
			log.Printf("Error storing message: %v", err)
			continue
		}

		log.Printf("Successfully stored message for: %s with status: %s", smsEvent.PhoneNumber, smsEvent.Status)
	}
}
