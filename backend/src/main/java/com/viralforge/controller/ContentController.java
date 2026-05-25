package com.viralforge.controller;

import com.viralforge.dto.request.ContentGenerationDTO;
import com.viralforge.dto.response.ApiResponseDTO;
import com.viralforge.dto.response.ContentGenerationResponseDTO;
import com.viralforge.entity.GeneratedContent;
import com.viralforge.entity.User;
import com.viralforge.repository.GeneratedContentRepository;
import com.viralforge.repository.UserRepository;
import com.viralforge.service.ai.AIOrchestratorService;
import com.viralforge.service.language.LanguageAdaptationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
public class ContentController {
    @Autowired
    private AIOrchestratorService aIOrchestratorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeneratedContentRepository generatedContentRepository;

    @Autowired
    private LanguageAdaptationService languageAdaptationService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponseDTO<ContentGenerationResponseDTO>> generateContent(
        @Valid @RequestBody ContentGenerationDTO request,
        Authentication authentication
    ) {
        log.info("Content generation request from user: {}", authentication.getName());

        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        ContentGenerationResponseDTO response = aIOrchestratorService.orchestrateContentGeneration(request, user);

        // Save updated user with API usage count
        userRepository.save(user);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(response, "Content generated successfully"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getContentHistory(
        Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<GeneratedContent> contents = generatedContentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<Map<String, Object>> history = contents.stream().map(content -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", content.getId());
            item.put("topicIdea", content.getContentRequest().getTopicIdea());
            item.put("platform", content.getContentRequest().getPlatform());
            item.put("niche", content.getContentRequest().getNiche());
            item.put("viralScore", content.getViralScore());
            item.put("confidenceScore", content.getConfidenceScore());
            item.put("primaryModelUsed", content.getPrimaryModelUsed());
            item.put("scriptContent", content.getScriptContent());
            item.put("createdAt", content.getCreatedAt());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(history, "Content history retrieved successfully"));
    }

    @PostMapping("/adapt-languages")
    public ResponseEntity<ApiResponseDTO<Object>> adaptContentToLanguages(
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        log.info("Language adaptation request from user: {}", authentication.getName());

        String originalContent = (String) request.get("scriptContent");
        String topic = (String) request.get("topicIdea");
        String platform = (String) request.get("platform");
        String niche = (String) request.get("niche");
        String targetAudience = (String) request.get("targetAudience");
        @SuppressWarnings("unchecked")
        List<String> languages = (List<String>) request.getOrDefault("languages", new ArrayList<>());

        Object adaptedContent = languageAdaptationService.adaptContentToLanguages(
            originalContent, topic, platform, niche, targetAudience, languages
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(adaptedContent, "Content adapted to multiple languages successfully"));
    }

    @GetMapping("/languages")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> getAvailableLanguages() {
        log.info("Fetching available languages");
        Map<String, String> languages = languageAdaptationService.getAvailableLanguages();
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(languages, "Available languages retrieved successfully"));
    }

    @PostMapping("/generate-subtitles")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> generateSubtitles(
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        log.info("Subtitle generation request from user: {}", authentication.getName());

        String content = (String) request.get("content");
        String languageCode = (String) request.getOrDefault("languageCode", "en");

        String subtitles = languageAdaptationService.generateSubtitles(content, languageCode);

        Map<String, Object> result = new HashMap<>();
        result.put("subtitles", subtitles);
        result.put("languageCode", languageCode);
        result.put("format", "SRT");

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result, "Subtitles generated successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponseDTO<String>> health() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success("OK", "Content service is healthy"));
    }
}
