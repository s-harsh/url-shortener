package com.urlshortener.service;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    Optional<String> getCachedUrl(String shortCode);

    void cacheUrl(String shortCode, String originalUrl, Duration ttl);

    void evictUrl(String shortCode);

    long incrementCounter();
}
