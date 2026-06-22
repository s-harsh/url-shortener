package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.exception.CustomAliasAlreadyExistsException;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.model.Url;
import com.urlshortener.repository.ClickRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.service.impl.UrlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock private UrlRepository urlRepository;
    @Mock private ClickRepository clickRepository;
    @Mock private CacheService cacheService;

    private AppProperties appProperties;
    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.setBaseUrl("http://localhost:8080");
        appProperties.setShortCodeLength(7);
        appProperties.setDefaultTtlDays(365);
        urlService = new UrlServiceImpl(urlRepository, clickRepository, cacheService, appProperties);
    }

    @Test
    void shortenUrl_valid_url_returns_response() {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setUrl("https://www.example.com/very/long/path");

        when(cacheService.incrementCounter()).thenReturn(12345L);
        when(urlRepository.existsByShortCode(any())).thenReturn(false);
        Url saved = Url.builder()
                .id(1L)
                .shortCode("3d7")
                .originalUrl("https://www.example.com/very/long/path")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(365))
                .build();
        when(urlRepository.save(any())).thenReturn(saved);

        ShortenUrlResponse response = urlService.shortenUrl(request, "127.0.0.1");

        assertThat(response.getShortUrl()).startsWith("http://localhost:8080/");
        assertThat(response.getOriginalUrl()).isEqualTo("https://www.example.com/very/long/path");
        verify(cacheService).cacheUrl(any(), eq("https://www.example.com/very/long/path"), any());
    }

    @Test
    void shortenUrl_invalid_url_throws_InvalidUrlException() {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setUrl("not-a-url");

        assertThatThrownBy(() -> urlService.shortenUrl(request, "127.0.0.1"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void shortenUrl_custom_alias_already_taken_throws() {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setUrl("https://example.com");
        request.setCustomAlias("my-link");

        when(urlRepository.existsByCustomAlias("my-link")).thenReturn(true);

        assertThatThrownBy(() -> urlService.shortenUrl(request, "127.0.0.1"))
                .isInstanceOf(CustomAliasAlreadyExistsException.class);
    }

    @Test
    void getOriginalUrl_cache_hit_returns_without_db_query() {
        when(cacheService.getCachedUrl("abc123")).thenReturn(Optional.of("https://example.com"));

        String result = urlService.getOriginalUrl("abc123");

        assertThat(result).isEqualTo("https://example.com");
        verifyNoInteractions(urlRepository);
    }

    @Test
    void getOriginalUrl_cache_miss_queries_db() {
        when(cacheService.getCachedUrl("abc123")).thenReturn(Optional.empty());
        Url url = Url.builder()
                .id(1L)
                .shortCode("abc123")
                .originalUrl("https://example.com")
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();
        when(urlRepository.findByShortCodeAndActiveTrue("abc123")).thenReturn(Optional.of(url));

        String result = urlService.getOriginalUrl("abc123");

        assertThat(result).isEqualTo("https://example.com");
        verify(cacheService).cacheUrl(eq("abc123"), eq("https://example.com"), any());
    }

    @Test
    void getOriginalUrl_not_found_throws_UrlNotFoundException() {
        when(cacheService.getCachedUrl("xyz")).thenReturn(Optional.empty());
        when(urlRepository.findByShortCodeAndActiveTrue("xyz")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.getOriginalUrl("xyz"))
                .isInstanceOf(UrlNotFoundException.class);
    }

    @Test
    void deleteUrl_not_found_throws_UrlNotFoundException() {
        when(urlRepository.findByShortCodeAndActiveTrue("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.deleteUrl("nonexistent"))
                .isInstanceOf(UrlNotFoundException.class);
    }

    @Test
    void deleteUrl_deactivates_and_evicts_cache() {
        Url url = Url.builder().id(1L).shortCode("abc").originalUrl("https://x.com").active(true).build();
        when(urlRepository.findByShortCodeAndActiveTrue("abc")).thenReturn(Optional.of(url));
        when(urlRepository.deactivateByShortCode("abc")).thenReturn(1);

        urlService.deleteUrl("abc");

        verify(urlRepository).deactivateByShortCode("abc");
        verify(cacheService).evictUrl("abc");
    }
}
