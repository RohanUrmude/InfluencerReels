package com.viralforge.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_content", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_is_favorited", columnList = "is_favorited"),
    @Index(name = "idx_is_published", columnList = "is_published"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "content_request_id", nullable = false)
    private ContentRequest contentRequest;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private JsonNode audienceAnalysis;

    @Column(columnDefinition = "TEXT")
    private String scriptContent;

    @Column(columnDefinition = "TEXT[]")
    private String[] hashtags;

    @Column(length = 200)
    private String thumbnailText;

    @Column(columnDefinition = "TEXT")
    private String captions;

    @Column(columnDefinition = "TEXT[]")
    private String[] seoHashtags;

    @Column(length = 500)
    private String postingSchedule;

    @Column(columnDefinition = "TEXT")
    private String engagementStrategy;

    @Column(columnDefinition = "TEXT")
    private String growthTips;

    @Column(length = 50)
    private String bestPostingTime;

    @Column(columnDefinition = "TEXT")
    private String platformOptimization;

    @Column(precision = 5, scale = 2)
    private BigDecimal viralScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(length = 255)
    private String primaryModelUsed;

    @Column(length = 255)
    private String fallbackModelUsed;

    @Column(columnDefinition = "TEXT")
    private String audienceType;

    @Column(columnDefinition = "TEXT")
    private String recommendedTone;

    @Column(columnDefinition = "TEXT")
    private String contentStyle;

    @Column(columnDefinition = "TEXT[]")
    private String[] engagementTriggers;

    @Column(columnDefinition = "TEXT")
    private String trendAlignment;

    @Column(columnDefinition = "TEXT[]")
    private String[] viralHooks;

    @Column(length = 200)
    private String recommendedCta;

    @Column(name = "generation_latency_ms")
    private Integer generationLatencyMs;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isFavorited = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isPublished = false;

    private LocalDateTime publishedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
