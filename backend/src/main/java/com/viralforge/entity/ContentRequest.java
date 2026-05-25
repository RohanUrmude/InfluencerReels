package com.viralforge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_requests", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String niche;

    @Column(nullable = false, length = 100)
    private String targetAudience;

    @Column(length = 100)
    private String vibe;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String topicIdea;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(columnDefinition = "TEXT")
    private String creatorGoal;

    @Column(length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'PENDING'")
    private String status = "PENDING";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
