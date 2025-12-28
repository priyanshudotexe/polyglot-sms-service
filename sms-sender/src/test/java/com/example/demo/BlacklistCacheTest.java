package com.example.demo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.example.demo.service.BlacklistCache;

@RunWith(MockitoJUnitRunner.class)
public class BlacklistCacheTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private BlacklistCache blacklistCache;

    @Before
    public void setUp() {
        blacklistCache = new BlacklistCache(redisTemplate);
    }

    @Test
    public void testIsBlacklistedReturnsTrueWhenPhoneNumberExists() {
        String phoneNumber = "+1234567890";
        when(redisTemplate.hasKey(phoneNumber)).thenReturn(true);

        assertTrue(blacklistCache.isBlacklisted(phoneNumber));
        verify(redisTemplate).hasKey(phoneNumber);
    }

    @Test
    public void testIsBlacklistedReturnsFalseWhenPhoneNumberDoesNotExist() {
        String phoneNumber = "+1234567890";
        when(redisTemplate.hasKey(phoneNumber)).thenReturn(false);

        assertFalse(blacklistCache.isBlacklisted(phoneNumber));
        verify(redisTemplate).hasKey(phoneNumber);
    }

    @Test
    public void testAddToBlacklist() {
        String phoneNumber = "+1234567890";
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        blacklistCache.addToBlacklist(phoneNumber);

        verify(redisTemplate).opsForValue();
        verify(valueOps).set(phoneNumber, "1");
    }

    @Test
    public void testRemoveFromBlacklist() {
        String phoneNumber = "+1234567890";

        blacklistCache.removeFromBlacklist(phoneNumber);

        verify(redisTemplate).delete(phoneNumber);
    }
}
