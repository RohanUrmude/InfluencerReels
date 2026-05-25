package com.viralforge.controller;

import com.viralforge.dto.response.ApiResponseDTO;
import com.viralforge.service.trending.TrendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/trending")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class TrendingController {

    @Autowired
    private TrendingService trendingService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<Object>> getTrendingContent() {
        log.info("Fetching all trending content");
        Object trendingData = trendingService.getTrendingContent();
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(trendingData, "Trending content retrieved successfully"));
    }

    @GetMapping("/{platform}")
    public ResponseEntity<ApiResponseDTO<Object>> getTrendingByPlatform(@PathVariable String platform) {
        log.info("Fetching trending content for platform: {}", platform);

        if (!isValidPlatform(platform)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error("INVALID_PLATFORM", "Invalid platform. Supported: tiktok, reels, youtube", 400));
        }

        Object trendingData = trendingService.getTrendingByPlatform(platform);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(trendingData, "Trending content for " + platform + " retrieved successfully"));
    }

    private boolean isValidPlatform(String platform) {
        return platform.equals("tiktok") || platform.equals("reels") || platform.equals("youtube");
    }
}
