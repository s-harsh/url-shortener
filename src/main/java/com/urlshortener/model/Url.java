package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls", indexes = {
        @Index(name = "idx_urls_short_code", columnList = "short_code", unique = true),
        @Index(name = "idx_urls_custom_alias", columnList = "custom_alias")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "url_seq")
    @SequenceGenerator(name = "url_seq", sequenceName = "url_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    @Column(name = "custom_alias", unique = true, length = 100)
    private String customAlias;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_by_ip", length = 45)
    private String createdByIp;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
