package com.urlshortener.controller;

import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ErrorResponse;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.dto.response.UrlInfoResponse;
import com.urlshortener.dto.response.UrlStatsResponse;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Tag(name = "URL Management", description = "Create, inspect, and delete short URLs")
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    @Operation(summary = "Shorten a URL", description = "Creates a short URL from a long URL")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "URL shortened successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid URL or request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Custom alias already taken",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShortenUrlResponse> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest httpRequest) {

        String ip = resolveClientIp(httpRequest);
        ShortenUrlResponse response = urlService.shortenUrl(request, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get URL info", description = "Returns metadata for a short URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL info returned"),
            @ApiResponse(responseCode = "404", description = "Short URL not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "410", description = "Short URL has expired",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UrlInfoResponse> getUrlInfo(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getUrlInfo(shortCode));
    }

    @GetMapping("/{shortCode}/stats")
    @Operation(summary = "Get URL analytics", description = "Returns click stats for a short URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats returned"),
            @ApiResponse(responseCode = "404", description = "Short URL not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getUrlStats(shortCode));
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Delete a short URL", description = "Soft-deletes a short URL")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "URL deleted"),
            @ApiResponse(responseCode = "404", description = "Short URL not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        urlService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null ? realIp : request.getRemoteAddr();
    }
}
