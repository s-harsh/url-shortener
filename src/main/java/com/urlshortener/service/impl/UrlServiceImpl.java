package com.urlshortener.service.impl;

import com.urlshortener.config.AppProperties;
import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.dto.response.UrlInfoResponse;
import com.urlshortener.dto.response.UrlStatsResponse;
import com.urlshortener.exception.CustomAliasAlreadyExistsException;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.model.Url;
import com.urlshortener.repository.ClickRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.service.CacheService;
import com.urlshortener.service.UrlService;
import com.urlshortener.util.Base62Encoder;
import com.urlshortener.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;
    private final CacheService cacheService;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request, String ipAddress) {
        String originalUrl = request.getUrl();
        if (!UrlValidator.isValid(originalUrl)) {
            throw new InvalidUrlException("URL must use http or https and have a valid host: " + originalUrl);
        }

        String shortCode;
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            shortCode = request.getCustomAlias().toLowerCase();
            if (urlRepository.existsByCustomAlias(shortCode)) {
                throw new CustomAliasAlreadyExistsException(shortCode);
            }
        } else {
            shortCode = generateUniqueShortCode();
        }

        int ttlDays = request.getTtlDays() != null ? request.getTtlDays() : appProperties.getDefaultTtlDays();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(ttlDays);

        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .expiresAt(expiresAt)
                .customAlias(request.getCustomAlias() != null ? request.getCustomAlias().toLowerCase() : null)
                .createdByIp(ipAddress)
                .build();

        url = urlRepository.saveAndFlush(url);

        // Write-through cache
        cacheService.cacheUrl(shortCode, originalUrl, Duration.ofDays(ttlDays));

        log.info("Shortened URL: shortCode={}, original={}", shortCode, originalUrl);

        return ShortenUrlResponse.builder()
                .shortCode(shortCode)
                .shortUrl(buildShortUrl(shortCode))
                .originalUrl(originalUrl)
                .createdAt(url.getCreatedAt())
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        // L1: Redis cache
        return cacheService.getCachedUrl(shortCode).orElseGet(() -> {
            // L2: Database
            Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(shortCode));

            if (url.isExpired()) {
                throw new UrlExpiredException(shortCode);
            }

            // Backfill cache
            Duration remaining = Duration.between(LocalDateTime.now(), url.getExpiresAt());
            if (!remaining.isNegative()) {
                cacheService.cacheUrl(shortCode, url.getOriginalUrl(), remaining);
            }

            return url.getOriginalUrl();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public UrlInfoResponse getUrlInfo(String shortCode) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return UrlInfoResponse.builder()
                .shortCode(url.getShortCode())
                .shortUrl(buildShortUrl(url.getShortCode()))
                .originalUrl(url.getOriginalUrl())
                .clickCount(url.getClickCount())
                .active(url.isActive())
                .expired(url.isExpired())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String shortCode) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> rawStats = clickRepository.findDailyClicksSince(url.getId(), since);

        List<UrlStatsResponse.DailyStats> dailyStats = rawStats.stream()
                .map(row -> UrlStatsResponse.DailyStats.builder()
                        .date(((java.sql.Date) row[0]).toLocalDate())
                        .clicks((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return UrlStatsResponse.builder()
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .totalClicks(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .dailyStats(dailyStats)
                .build();
    }

    @Override
    @Transactional
    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        urlRepository.deactivateByShortCode(shortCode);
        cacheService.evictUrl(shortCode);

        log.info("Deleted (deactivated) URL: shortCode={}", shortCode);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String generateUniqueShortCode() {
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            long counter = cacheService.incrementCounter();
            String candidate = Base62Encoder.encode(counter);
            // Pad or truncate to configured length
            candidate = candidate.length() >= appProperties.getShortCodeLength()
                    ? candidate.substring(candidate.length() - appProperties.getShortCodeLength())
                    : candidate;
            if (!urlRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        // Fallback: use high-entropy random segment
        return Base62Encoder.encode(System.nanoTime() ^ (long)(Math.random() * Long.MAX_VALUE));
    }

    private String buildShortUrl(String shortCode) {
        return appProperties.getBaseUrl() + "/" + shortCode;
    }
}
