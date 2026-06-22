package com.urlshortener.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Analytics stats for a shortened URL")
public class UrlStatsResponse {

    private String shortCode;
    private String originalUrl;
    private Long totalClicks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    private List<DailyStats> dailyStats;

    @Data
    @Builder
    public static class DailyStats {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private Long clicks;
    }
}
