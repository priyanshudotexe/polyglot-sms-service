package com.example.demo;

import com.example.demo.service.TwillioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TwillioServiceTest {

    @InjectMocks
    private TwillioService twillioService;

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
    void testSendSms_Success() {
        // Arrange
        String phoneNumber = "+1234567890";
        String message = "Hello World";

        // Act
        twillioService.sendSms(phoneNumber, message);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Sending SMS to " + phoneNumber));
        assertTrue(output.contains(message));
    }

    @Test
    void testSendSms_WithDifferentPhoneNumber() {
        // Arrange
        String phoneNumber = "+9876543210";
        String message = "Test message";

        // Act
        twillioService.sendSms(phoneNumber, message);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains(phoneNumber));
        assertTrue(output.contains(message));
    }

    @Test
    void testSendSms_WithEmptyMessage() {
        // Arrange
        String phoneNumber = "+1234567890";
        String message = "";

        // Act
        twillioService.sendSms(phoneNumber, message);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Sending SMS to " + phoneNumber));
    }

    @Test
    void testSendSms_WithLongMessage() {
        // Arrange
        String phoneNumber = "+1234567890";
        String message = "This is a very long message that exceeds the typical SMS length limit to test how the service handles long messages";

        // Act
        twillioService.sendSms(phoneNumber, message);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Sending SMS to " + phoneNumber));
        assertTrue(output.contains(message));
    }

    @Test
    void testConstructor() {
        // Act
        TwillioService service = new TwillioService();

        // Assert
        assertNotNull(service);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }
}
