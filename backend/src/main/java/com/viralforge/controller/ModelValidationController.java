package com.viralforge.controller;

import com.viralforge.service.validation.ModelValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/models/validation")
public class ModelValidationController {
    @Autowired
    private ModelValidationService modelValidationService;

    /**
     * Validate a model response using Claude
     * POST /api/models/validation/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateModelResponse(
            @RequestParam String modelName,
            @RequestParam String prompt,
            @RequestBody String modelResponse) {
        try {
            String evaluation = modelValidationService.validateResponseWithClaude(modelName, prompt, modelResponse);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("modelName", modelName);
            response.put("evaluation", evaluation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating model response: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Score a model response on a scale of 1-10
     * POST /api/models/validation/score
     */
    @PostMapping("/score")
    public ResponseEntity<Map<String, Object>> scoreModelResponse(
            @RequestParam String modelName,
            @RequestParam String context,
            @RequestBody String responseContent) {
        try {
            var score = modelValidationService.scoreModelResponse(modelName, responseContent, context);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("modelName", modelName);
            response.put("score", score);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error scoring model response: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Compare responses from multiple models
     * POST /api/models/validation/compare
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareModels(
            @RequestParam String prompt,
            @RequestBody Map<String, String> modelResponses) {
        try {
            Map<String, Object> comparison = modelValidationService.compareModelResponses(modelResponses, prompt);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comparison", comparison);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error comparing models: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get performance report for a specific model
     * GET /api/models/validation/performance/{modelName}
     */
    @GetMapping("/performance/{modelName}")
    public ResponseEntity<Map<String, Object>> getModelPerformance(@PathVariable String modelName) {
        try {
            Map<String, Object> report = modelValidationService.getModelPerformanceReport(modelName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", report);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving performance report: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get performance metrics for all models
     * GET /api/models/validation/performance/all
     */
    @GetMapping("/performance/all")
    public ResponseEntity<Map<String, Object>> getAllModelsPerformance() {
        try {
            Map<String, Object> allModels = modelValidationService.getAllModelsPerformance();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("models", allModels);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all models performance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Record model response metrics
     * POST /api/models/validation/record
     */
    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> recordModelMetrics(
            @RequestParam String modelName,
            @RequestParam(required = false) Integer tokensUsed,
            @RequestBody String responseContent) {
        try {
            modelValidationService.recordModelResponse(modelName, responseContent, tokensUsed);
            modelValidationService.savePerformanceMetrics(modelName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Metrics recorded for model: " + modelName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recording metrics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Clear metrics for new session
     * POST /api/models/validation/clear
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearMetrics() {
        try {
            modelValidationService.clearMetrics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All metrics cleared for new session"
            ));
        } catch (Exception e) {
            log.error("Error clearing metrics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
