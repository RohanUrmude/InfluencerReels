package com.viralforge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_performance_logs", indexes = {
    @Index(name = "idx_is_healthy", columnList = "is_healthy")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelPerformanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String modelName;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer requestCount = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer successCount = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer failureCount = 0;

    private Integer averageLatencyMs;

    private Integer averageTokensUsed;

    private LocalDateTime lastUsed;

    @Column(precision = 5, scale = 2)
    private BigDecimal reliabilityScore;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isHealthy = true;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
}
