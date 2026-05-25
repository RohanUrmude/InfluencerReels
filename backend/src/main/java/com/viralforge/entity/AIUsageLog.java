package com.viralforge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_model_name", columnList = "model_name"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIUsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "content_request_id")
    private ContentRequest contentRequest;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Column(length = 50)
    private String modelType;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;

    private Integer latencyMs;

    @Column(precision = 10, scale = 6)
    private BigDecimal costEstimate;

    @Column(length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer retryCount = 0;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean fallbackUsed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
