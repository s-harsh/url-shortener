package com.urlshortener.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response after successfully shortening a URL")
public class ShortenUrlResponse {

    @Schema(description = "Generated short code", example = "aB3xY7k")
    private String shortCode;

    @Schema(description = "Full short URL", example = "https://short.ly/aB3xY7k")
    private String shortUrl;

    @Schema(description = "Original URL that was shortened", example = "https://www.example.com/very/long/path")
    private String originalUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the short URL was created")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When the short URL expires (null = never)")
    private LocalDateTime expiresAt;
}
