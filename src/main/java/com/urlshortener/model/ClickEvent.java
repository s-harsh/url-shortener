package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_events", indexes = {
        @Index(name = "idx_click_events_url_id", columnList = "url_id"),
        @Index(name = "idx_click_events_clicked_at", columnList = "clicked_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "click_seq")
    @SequenceGenerator(name = "click_seq", sequenceName = "click_event_id_seq", allocationSize = 100)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(name = "clicked_at", nullable = false)
    private LocalDateTime clickedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "referer", length = 2048)
    private String referer;
}
