package com.urlshortener.controller;

import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect", description = "Redirect short URLs to their original destinations")
public class RedirectController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode:[a-zA-Z0-9-]+}")
    @Operation(summary = "Redirect short URL",
               description = "Resolves a short code and redirects (302) to the original URL")
    @ApiResponse(responseCode = "302", description = "Redirect to original URL")
    @ApiResponse(responseCode = "404", description = "Short URL not found")
    @ApiResponse(responseCode = "410", description = "Short URL has expired")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String originalUrl = urlService.getOriginalUrl(shortCode);

        // Fire-and-forget analytics — async, never blocks the redirect
        String ip = resolveClientIp(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String referer = request.getHeader(HttpHeaders.REFERER);
        analyticsService.recordClick(shortCode, ip, userAgent, referer);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .header("X-Short-Code", shortCode)
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null ? realIp : request.getRemoteAddr();
    }
}
