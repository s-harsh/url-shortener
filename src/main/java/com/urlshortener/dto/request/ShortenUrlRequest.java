package com.urlshortener.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Schema(description = "Request body to shorten a URL")
public class ShortenUrlRequest {

    @NotBlank(message = "URL must not be blank")
    @URL(message = "Must be a valid URL (include http:// or https://)")
    @Size(max = 2048, message = "URL must be at most 2048 characters")
    @Schema(description = "The original URL to shorten", example = "https://www.example.com/very/long/path")
    private String url;

    @Pattern(regexp = "^[a-zA-Z0-9-]{3,20}$",
             message = "Custom alias must be 3–20 characters: letters, numbers, and hyphens only")
    @Schema(description = "Optional custom alias (3-20 chars, alphanumeric + hyphens)", example = "my-link")
    private String customAlias;

    @Min(value = 1, message = "TTL must be at least 1 day")
    @Max(value = 365, message = "TTL must be at most 365 days")
    @Schema(description = "Time-to-live in days (1-365). Defaults to 365.", example = "30")
    private Integer ttlDays;
}
