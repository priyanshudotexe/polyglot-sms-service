package com.example.demo.service;
import org.springframework.stereotype.Service;
import com.example.demo.service.BlacklistCache;
import com.example.demo.model.SmsRequest;
import com.example.demo.model.SmsEvent;
@Service
public class SmsService {
    private final BlacklistCache cache;
    private final SmsEventProducer eventProducer;
    private final TwillioService twillioService;
    public SmsService(BlacklistCache cache, SmsEventProducer eventProducer, TwillioService twillioService) {
        this.cache = cache;
        this.eventProducer = eventProducer;
        this.twillioService = twillioService;
    }    
    public String sendSms(SmsRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String message = request.getMessage();
        if(cache.isBlacklisted(phoneNumber)) {
            SmsEvent event = new SmsEvent(phoneNumber, message, "Failed: Phone number is blacklisted");
            eventProducer.sendSmsEvent(event);
            return "Failed: Phone number is blacklisted";
        }
        twillioService.sendSms(phoneNumber, message);
        SmsEvent event = new SmsEvent(phoneNumber, message, "Sent");
        eventProducer.sendSmsEvent(event);
        return "SMS sent to " + request.getPhoneNumber();
    }
}
