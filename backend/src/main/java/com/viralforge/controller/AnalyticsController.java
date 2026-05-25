package com.viralforge.controller;

import com.viralforge.entity.GeneratedContent;
import com.viralforge.entity.User;
import com.viralforge.dto.response.ApiResponseDTO;
import com.viralforge.repository.GeneratedContentRepository;
import com.viralforge.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
public class AnalyticsController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeneratedContentRepository generatedContentRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getAnalyticsDashboard(
        Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<GeneratedContent> contents = generatedContentRepository.findByUserId(user.getId());

        Map<String, Object> analytics = new HashMap<>();

        int totalGenerated = contents.size();
        analytics.put("totalGenerated", totalGenerated);

        double avgViralScore = totalGenerated > 0 ?
            contents.stream()
                .map(c -> c.getViralScore() != null ? c.getViralScore().doubleValue() : 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0) : 0;
        analytics.put("avgViralScore", avgViralScore);

        double avgConfidence = totalGenerated > 0 ?
            contents.stream()
                .map(c -> c.getConfidenceScore() != null ? c.getConfidenceScore().doubleValue() : 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0) : 0;
        analytics.put("avgConfidence", avgConfidence);

        long educationalCount = contents.stream()
            .filter(c -> "educational".equalsIgnoreCase(c.getContentRequest().getContentType()))
            .count();
        long entertainmentCount = contents.stream()
            .filter(c -> "entertainment".equalsIgnoreCase(c.getContentRequest().getContentType()))
            .count();

        analytics.put("educationalCount", educationalCount);
        analytics.put("entertainmentCount", entertainmentCount);

        Map<String, Long> platformCounts = contents.stream()
            .collect(Collectors.groupingBy(
                c -> c.getContentRequest().getPlatform(),
                Collectors.counting()
            ));

        List<Map<String, Object>> platformStats = platformCounts.entrySet().stream()
            .map(entry -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", entry.getKey());
                stat.put("count", entry.getValue());
                stat.put("percentage", totalGenerated > 0 ? (entry.getValue() * 100.0 / totalGenerated) : 0);
                return stat;
            })
            .collect(Collectors.toList());
        analytics.put("platformStats", platformStats);

        Map<String, Integer> modelStats = new HashMap<>();
        modelStats.put("phi", totalGenerated);
        modelStats.put("llama", (int) contents.stream()
            .filter(c -> c.getPrimaryModelUsed() != null && c.getPrimaryModelUsed().contains("Llama"))
            .count());
        modelStats.put("mistral", (int) contents.stream()
            .filter(c -> c.getPrimaryModelUsed() != null && c.getPrimaryModelUsed().contains("Mistral"))
            .count());
        analytics.put("modelStats", modelStats);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(analytics, "Analytics retrieved successfully"));
    }
}
