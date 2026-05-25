package com.viralforge.repository;

import com.viralforge.entity.AIUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIUsageLogRepository extends JpaRepository<AIUsageLog, Long> {
    List<AIUsageLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AIUsageLog> findByModelNameAndStatus(String modelName, String status);
    long countByUserId(Long userId);
    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime dateTime);

    @Query("SELECT COALESCE(SUM(a.totalTokens), 0) FROM AIUsageLog a WHERE a.user.id = :userId AND a.createdAt >= :startDate")
    Long getTotalTokensUsedByUserSince(Long userId, LocalDateTime startDate);
}
