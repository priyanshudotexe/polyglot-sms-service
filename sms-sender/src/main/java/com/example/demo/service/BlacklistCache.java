package com.example.demo.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service    
public class BlacklistCache {
    private final StringRedisTemplate redisTemplate;
    public BlacklistCache(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    public boolean isBlacklisted(String phoneNumber) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(phoneNumber));
    }
    public void addToBlacklist(String phoneNumber) {
        redisTemplate.opsForValue().set(phoneNumber, "1");
    }
    public void removeFromBlacklist(String phoneNumber) {
        redisTemplate.delete(phoneNumber);
    }
}
