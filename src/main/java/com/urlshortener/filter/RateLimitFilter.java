package com.urlshortener.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.config.AppProperties;
import com.urlshortener.dto.response.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    // Per-IP bucket store — in production, use Redis-backed Bucket4j for multi-instance deployments
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", ip);
            writeRateLimitError(response, request.getRequestURI(), ip);
        }
    }

    private Bucket newBucket(String ip) {
        int maxRequests = appProperties.getRateLimit().getMaxRequests();
        int windowMinutes = appProperties.getRateLimit().getWindowMinutes();

        Bandwidth limit = Bandwidth.builder()
                .capacity(maxRequests)
                .refillIntervally(maxRequests, Duration.ofMinutes(windowMinutes))
                .build();

        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null ? realIp : request.getRemoteAddr();
    }

    private void writeRateLimitError(HttpServletResponse response, String path, String ip)
            throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message("Rate limit exceeded. Please slow down.")
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Exclude health checks and docs from rate limiting
        return path.startsWith("/actuator") || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs") || path.equals("/favicon.ico");
    }
}
