package com.viralforge.repository;

import com.viralforge.entity.ContentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentRequestRepository extends JpaRepository<ContentRequest, Long> {
    Page<ContentRequest> findByUserId(Long userId, Pageable pageable);
    List<ContentRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, String status);
}
