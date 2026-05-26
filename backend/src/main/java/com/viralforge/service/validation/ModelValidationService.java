package com.viralforge.service.validation;

import com.viralforge.entity.ModelPerformanceLog;
import com.viralforge.repository.ModelPerformanceLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ModelValidationService {
    @Autowired
    private ModelPerformanceLogRepository modelPerformanceLogRepository;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    // Store metrics for each model in current session
    private Map<String, ModelMetrics> modelMetricsMap = new HashMap<>();

    public static class ModelMetrics {
        public String modelName;
        public long startTime;
        public long endTime;
        public String responseContent;
        public Integer tokensUsed;
        public String evaluationScore;
        public String validationNotes;

        public ModelMetrics(String modelName) {
            this.modelName = modelName;
            this.startTime = System.currentTimeMillis();
        }
    }

    /**
     * Start tracking a model's performance
     */
    public ModelMetrics startModelTracking(String modelName) {
        ModelMetrics metrics = new ModelMetrics(modelName);
        modelMetricsMap.put(modelName, metrics);
        log.info("Started tracking model: {}", modelName);
        return metrics;
    }

    /**
     * End tracking and record response
     */
    public void recordModelResponse(String modelName, String responseContent, Integer tokensUsed) {
        ModelMetrics metrics = modelMetricsMap.getOrDefault(modelName, new ModelMetrics(modelName));
        metrics.endTime = System.currentTimeMillis();
        metrics.responseContent = responseContent;
        metrics.tokensUsed = tokensUsed;
        modelMetricsMap.put(modelName, metrics);
        log.info("Recorded response for model: {} (tokens: {})", modelName, tokensUsed);
    }

    /**
     * Validate model response using Gemini
     */
    public String validateResponseWithClaude(String modelName, String originalPrompt, String modelResponse) {
        try {
            String validationPrompt = buildValidationPrompt(modelName, originalPrompt, modelResponse);
            String claudeEvaluation = callGeminiAPI(validationPrompt);

            ModelMetrics metrics = modelMetricsMap.getOrDefault(modelName, new ModelMetrics(modelName));
            metrics.validationNotes = claudeEvaluation;
            modelMetricsMap.put(modelName, metrics);

            log.info("Claude validation completed for {}: {}", modelName, claudeEvaluation.substring(0, Math.min(100, claudeEvaluation.length())));
            return claudeEvaluation;
        } catch (Exception e) {
            log.error("Error validating response with Claude for model {}: {}", modelName, e.getMessage());
            return "Validation failed: " + e.getMessage();
        }
    }

    /**
     * Score model response quality (1-10)
     */
    public BigDecimal scoreModelResponse(String modelName, String responseContent, String context) {
        try {
            String scoringPrompt = buildScoringPrompt(modelName, responseContent, context);
            String scoreResponse = callGeminiAPI(scoringPrompt);

            // Extract score from response (expecting format: "Score: X/10")
            BigDecimal score = extractScore(scoreResponse);

            ModelMetrics metrics = modelMetricsMap.getOrDefault(modelName, new ModelMetrics(modelName));
            metrics.evaluationScore = score.toString();
            modelMetricsMap.put(modelName, metrics);

            log.info("Model {} scored: {}/10", modelName, score);
            return score;
        } catch (Exception e) {
            log.error("Error scoring response for model {}: {}", modelName, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Compare responses from multiple models
     */
    public Map<String, Object> compareModelResponses(Map<String, String> modelResponses, String prompt) {
        Map<String, Object> comparison = new HashMap<>();
        Map<String, String> evaluations = new HashMap<>();

        try {
            for (Map.Entry<String, String> entry : modelResponses.entrySet()) {
                String evaluation = validateResponseWithClaude(entry.getKey(), prompt, entry.getValue());
                evaluations.put(entry.getKey(), evaluation);
            }

            // Get comparative analysis from Gemini
            String comparativePrompt = buildComparativePrompt(modelResponses, prompt);
            String comparativeAnalysis = callGeminiAPI(comparativePrompt);

            comparison.put("individual_evaluations", evaluations);
            comparison.put("comparative_analysis", comparativeAnalysis);
            comparison.put("timestamp", LocalDateTime.now());

            log.info("Model comparison completed. Analyzed {} models", modelResponses.size());
        } catch (Exception e) {
            log.error("Error comparing model responses: {}", e.getMessage());
            comparison.put("error", e.getMessage());
        }

        return comparison;
    }

    /**
     * Save performance metrics to database
     */
    public void savePerformanceMetrics(String modelName) {
        try {
            ModelMetrics metrics = modelMetricsMap.get(modelName);
            if (metrics == null) {
                log.warn("No metrics found for model: {}", modelName);
                return;
            }

            long latency = metrics.endTime - metrics.startTime;

            ModelPerformanceLog existingLog = modelPerformanceLogRepository
                .findByModelName(modelName)
                .orElse(null);

            if (existingLog == null) {
                // Create new performance log
                ModelPerformanceLog newLog = ModelPerformanceLog.builder()
                    .modelName(modelName)
                    .requestCount(1)
                    .successCount(1)
                    .failureCount(0)
                    .averageLatencyMs((int) latency)
                    .averageTokensUsed(metrics.tokensUsed)
                    .lastUsed(LocalDateTime.now())
                    .reliabilityScore(new BigDecimal("1.00"))
                    .isHealthy(true)
                    .build();
                modelPerformanceLogRepository.save(newLog);
                log.info("Created new performance log for model: {}", modelName);
            } else {
                // Update existing log
                int totalRequests = existingLog.getRequestCount() + 1;
                int totalSuccesses = existingLog.getSuccessCount() + 1;

                int avgLatency = (int) ((existingLog.getAverageLatencyMs() * existingLog.getRequestCount() + latency) / totalRequests);
                int avgTokens = (int) ((existingLog.getAverageTokensUsed() * existingLog.getRequestCount() + metrics.tokensUsed) / totalRequests);

                BigDecimal reliability = new BigDecimal(totalSuccesses).divide(
                    new BigDecimal(totalRequests), 2, BigDecimal.ROUND_HALF_UP
                );

                existingLog.setRequestCount(totalRequests);
                existingLog.setSuccessCount(totalSuccesses);
                existingLog.setAverageLatencyMs(avgLatency);
                existingLog.setAverageTokensUsed(avgTokens);
                existingLog.setLastUsed(LocalDateTime.now());
                existingLog.setReliabilityScore(reliability);
                existingLog.setIsHealthy(reliability.compareTo(new BigDecimal("0.80")) >= 0);

                modelPerformanceLogRepository.save(existingLog);
                log.info("Updated performance log for model: {} - Avg Latency: {}ms, Reliability: {}%",
                    modelName, avgLatency, reliability.multiply(new BigDecimal("100")));
            }
        } catch (Exception e) {
            log.error("Error saving performance metrics for model {}: {}", modelName, e.getMessage());
        }
    }

    /**
     * Get performance report for a model
     */
    public Map<String, Object> getModelPerformanceReport(String modelName) {
        Map<String, Object> report = new HashMap<>();

        try {
            var log = modelPerformanceLogRepository.findByModelName(modelName);
            if (log.isPresent()) {
                ModelPerformanceLog perf = log.get();
                report.put("modelName", perf.getModelName());
                report.put("totalRequests", perf.getRequestCount());
                report.put("successCount", perf.getSuccessCount());
                report.put("failureCount", perf.getFailureCount());
                report.put("averageLatencyMs", perf.getAverageLatencyMs());
                report.put("averageTokensUsed", perf.getAverageTokensUsed());
                report.put("reliabilityScore", perf.getReliabilityScore());
                report.put("isHealthy", perf.getIsHealthy());
                report.put("lastUsed", perf.getLastUsed());
            } else {
                report.put("message", "No performance data found for model: " + modelName);
            }
        } catch (Exception e) {
            log.error("Error retrieving performance report for model {}: {}", modelName, e.getMessage());
            report.put("error", e.getMessage());
        }

        return report;
    }

    /**
     * Get performance comparison across all models
     */
    public Map<String, Object> getAllModelsPerformance() {
        Map<String, Object> allModels = new HashMap<>();
        List<String> modelNames = Arrays.asList(
            "mistralai/Mistral-7B-Instruct-v0.2",
            "meta-llama/Meta-Llama-3-8B-Instruct",
            "microsoft/Phi-3-mini-4k-instruct"
        );

        for (String modelName : modelNames) {
            allModels.put(modelName, getModelPerformanceReport(modelName));
        }

        return allModels;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String buildValidationPrompt(String modelName, String originalPrompt, String modelResponse) {
        return String.format("""
            You are an expert AI content evaluator. Evaluate the response from the %s model.

            ORIGINAL PROMPT:
            %s

            MODEL RESPONSE:
            %s

            Please provide:
            1. Overall quality assessment
            2. Relevance to the prompt
            3. Accuracy and factual correctness
            4. Clarity and readability
            5. Suggestions for improvement

            Be concise but thorough.
            """, modelName, originalPrompt, modelResponse);
    }

    private String buildScoringPrompt(String modelName, String responseContent, String context) {
        return String.format("""
            Evaluate the following response from the %s model on a scale of 1-10.

            CONTEXT: %s
            RESPONSE: %s

            Provide your scoring in the format: "Score: X/10" followed by brief justification.
            """, modelName, context, responseContent);
    }

    private String buildComparativePrompt(Map<String, String> modelResponses, String prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("Compare the following responses to the same prompt:\n\n");
        sb.append("PROMPT: ").append(prompt).append("\n\n");

        for (Map.Entry<String, String> entry : modelResponses.entrySet()) {
            sb.append(entry.getKey()).append(":\n").append(entry.getValue()).append("\n\n");
        }

        sb.append("""
            Provide a comparative analysis including:
            1. Which response is best and why
            2. Strengths and weaknesses of each
            3. Use case recommendations for each model
            """);

        return sb.toString();
    }

    private String callGeminiAPI(String prompt) {
        try {
            if (geminiApiKey == null || geminiApiKey.isEmpty() || restTemplate == null) {
                log.warn("Gemini API key or RestTemplate not configured. Returning mock evaluation.");
                return "Mock evaluation: Response appears well-structured and relevant.";
            }

            String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;

            Map<String, Object> requestBody = new HashMap<>();

            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();

            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);

            content.put("parts", parts);
            contents.add(content);

            requestBody.put("contents", contents);

            var response = restTemplate.postForObject(urlWithKey, requestBody, Map.class);

            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> candidateContent = (Map<String, Object>) candidate.get("content");
                        if (candidateContent.containsKey("parts")) {
                            List<Map<String, String>> candidateParts = (List<Map<String, String>>) candidateContent.get("parts");
                            if (!candidateParts.isEmpty()) {
                                return candidateParts.get(0).get("text");
                            }
                        }
                    }
                }
            }

            return "Unable to retrieve Gemini evaluation";
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw new RuntimeException("Gemini API call failed", e);
        }
    }

    private BigDecimal extractScore(String scoreResponse) {
        try {
            // Look for pattern "Score: X/10"
            String[] parts = scoreResponse.split("Score:");
            if (parts.length > 1) {
                String scorePart = parts[1].split("/")[0].trim();
                return new BigDecimal(scorePart);
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error extracting score from response: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Clear metrics for new session
     */
    public void clearMetrics() {
        modelMetricsMap.clear();
        log.info("Cleared all metrics for new session");
    }
}
