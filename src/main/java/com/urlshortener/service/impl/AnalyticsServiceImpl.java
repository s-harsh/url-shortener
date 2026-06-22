package com.urlshortener.service.impl;

import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.Url;
import com.urlshortener.repository.ClickRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    @Async
    @Override
    @Transactional
    public void recordClick(String shortCode, String ipAddress, String userAgent, String referer) {
        try {
            Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode).orElse(null);
            if (url == null) return;

            ClickEvent event = ClickEvent.builder()
                    .url(url)
                    .clickedAt(LocalDateTime.now())
                    .ipAddress(sanitize(ipAddress, 45))
                    .userAgent(sanitize(userAgent, 512))
                    .referer(sanitize(referer, 2048))
                    .build();

            clickRepository.save(event);
            urlRepository.incrementClickCount(url.getId());
        } catch (Exception e) {
            log.error("Failed to record click for shortCode={}: {}", shortCode, e.getMessage());
        }
    }

    private String sanitize(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
