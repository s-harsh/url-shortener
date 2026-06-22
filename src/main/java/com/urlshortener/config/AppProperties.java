package com.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl = "http://localhost:8080";
    private int shortCodeLength = 7;
    private int defaultTtlDays = 365;

    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {
        private int maxRequests = 100;
        private int windowMinutes = 1;
    }
}
