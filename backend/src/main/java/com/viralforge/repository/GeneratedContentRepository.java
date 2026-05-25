package com.viralforge.repository;

import com.viralforge.entity.GeneratedContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GeneratedContentRepository extends JpaRepository<GeneratedContent, Long> {
    Page<GeneratedContent> findByUserId(Long userId, Pageable pageable);
    Page<GeneratedContent> findByUserIdAndIsFavoritedTrue(Long userId, Pageable pageable);
    Page<GeneratedContent> findByUserIdAndIsPublishedTrue(Long userId, Pageable pageable);
    List<GeneratedContent> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<GeneratedContent> findByUserId(Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndIsFavoritedTrue(Long userId);
    long countByUserIdAndIsPublishedTrue(Long userId);
}
