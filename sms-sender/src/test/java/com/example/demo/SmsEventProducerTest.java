package com.example.demo;

import com.example.demo.model.SmsEvent;
import com.example.demo.service.SmsEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsEventProducerTest {

    @Mock
    private KafkaTemplate<String, SmsEvent> kafkaTemplate;

    @InjectMocks
    private SmsEventProducer smsEventProducer;

    @Captor
    private ArgumentCaptor<SmsEvent> smsEventCaptor;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        // Capture System.out for testing console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    void testSendSmsEvent_Success() {
        // Arrange
        SmsEvent smsEvent = new SmsEvent("+1234567890", "Test message", "Sent");

        // Act
        smsEventProducer.sendSmsEvent(smsEvent);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("sms_events"), smsEventCaptor.capture());

        SmsEvent capturedEvent = smsEventCaptor.getValue();
        assertEquals("+1234567890", capturedEvent.getPhoneNumber());
        assertEquals("Test message", capturedEvent.getMessage());
        assertEquals("Sent", capturedEvent.getStatus());

        String output = outputStream.toString();
        assertTrue(output.contains("Produced SMS Event:"));
    }

    @Test
    void testSendSmsEvent_WithFailedStatus() {
        // Arrange
        SmsEvent smsEvent = new SmsEvent("+9876543210", "Failed message", "Failed");

        // Act
        smsEventProducer.sendSmsEvent(smsEvent);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("sms_events"), smsEventCaptor.capture());

        SmsEvent capturedEvent = smsEventCaptor.getValue();
        assertEquals("+9876543210", capturedEvent.getPhoneNumber());
        assertEquals("Failed message", capturedEvent.getMessage());
        assertEquals("Failed", capturedEvent.getStatus());
    }

    @Test
    void testSendSmsEvent_WithBlacklistedStatus() {
        // Arrange
        SmsEvent smsEvent = new SmsEvent("+1111111111", "Blocked", "Blacklisted");

        // Act
        smsEventProducer.sendSmsEvent(smsEvent);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("sms_events"), smsEventCaptor.capture());

        SmsEvent capturedEvent = smsEventCaptor.getValue();
        assertEquals("+1111111111", capturedEvent.getPhoneNumber());
        assertEquals("Blocked", capturedEvent.getMessage());
        assertEquals("Blacklisted", capturedEvent.getStatus());
    }

    @Test
    void testSendSmsEvent_WithEventId() {
        // Arrange
        SmsEvent smsEvent = new SmsEvent("+1234567890", "Test", "Sent");
        smsEvent.setEventId("evt-12345");

        // Act
        smsEventProducer.sendSmsEvent(smsEvent);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("sms_events"), smsEventCaptor.capture());

        SmsEvent capturedEvent = smsEventCaptor.getValue();
        assertEquals("evt-12345", capturedEvent.getEventId());
    }

    @Test
    void testSendSmsEvent_MultipleEvents() {
        // Arrange
        SmsEvent event1 = new SmsEvent("+1111111111", "Message 1", "Sent");
        SmsEvent event2 = new SmsEvent("+2222222222", "Message 2", "Sent");
        SmsEvent event3 = new SmsEvent("+3333333333", "Message 3", "Failed");

        // Act
        smsEventProducer.sendSmsEvent(event1);
        smsEventProducer.sendSmsEvent(event2);
        smsEventProducer.sendSmsEvent(event3);

        // Assert
        verify(kafkaTemplate, times(3)).send(eq("sms_events"), any(SmsEvent.class));
    }

    @Test
    void testSendSmsEvent_VerifyTopicName() {
        // Arrange
        SmsEvent smsEvent = new SmsEvent("+1234567890", "Test", "Sent");

        // Act
        smsEventProducer.sendSmsEvent(smsEvent);

        // Assert
        verify(kafkaTemplate).send(eq("sms_events"), any(SmsEvent.class));
    }

    @Test
    void testConstructor() {
        // Act
        SmsEventProducer producer = new SmsEventProducer(kafkaTemplate);

        // Assert
        assertNotNull(producer);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }
}
