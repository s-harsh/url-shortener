package com.urlshortener.service.impl;

import com.urlshortener.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private static final String URL_KEY_PREFIX = "url:";
    private static final String COUNTER_KEY = "url:counter";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<String> getCachedUrl(String shortCode) {
        try {
            String value = redisTemplate.opsForValue().get(URL_KEY_PREFIX + shortCode);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.warn("Redis get failed for shortCode={}: {}", shortCode, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void cacheUrl(String shortCode, String originalUrl, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(URL_KEY_PREFIX + shortCode, originalUrl, ttl);
        } catch (Exception e) {
            log.warn("Redis set failed for shortCode={}: {}", shortCode, e.getMessage());
        }
    }

    @Override
    public void evictUrl(String shortCode) {
        try {
            redisTemplate.delete(URL_KEY_PREFIX + shortCode);
        } catch (Exception e) {
            log.warn("Redis delete failed for shortCode={}: {}", shortCode, e.getMessage());
        }
    }

    @Override
    public long incrementCounter() {
        try {
            Long value = redisTemplate.opsForValue().increment(COUNTER_KEY);
            return value != null ? value : System.nanoTime();
        } catch (Exception e) {
            log.warn("Redis counter increment failed, falling back to nanoTime: {}", e.getMessage());
            return System.nanoTime();
        }
    }
}
