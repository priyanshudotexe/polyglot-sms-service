package com.example.demo.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.example.demo.model.SmsEvent;
@Service
public class SmsEventProducer {
    private final KafkaTemplate<String, SmsEvent> kafkaTemplate;
    private static final String TOPIC = "sms_events";
    public SmsEventProducer(KafkaTemplate<String, SmsEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }   

    public void sendSmsEvent(SmsEvent smsEvent) {
        kafkaTemplate.send(TOPIC, smsEvent);
        System.out.println("Produced SMS Event: " + smsEvent);
    }
}
