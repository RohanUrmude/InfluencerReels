package com.viralforge.service.ai;

import com.viralforge.entity.ModelPerformanceLog;
import com.viralforge.repository.ModelPerformanceLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AIRouterService {
    @Autowired
    private ModelPerformanceLogRepository modelPerformanceLogRepository;

    private static final String PHI_MODEL = "microsoft/Phi-3-mini-4k-instruct";
    private static final String LLAMA_MODEL = "meta-llama/Meta-Llama-3-8B-Instruct";
    private static final String MISTRAL_MODEL = "mistralai/Mistral-7B-Instruct-v0.2";

    public enum ModelType {
        AUDIENCE_ANALYZER("PHI", PHI_MODEL),
        EDUCATIONAL_GENERATOR("LLAMA", LLAMA_MODEL),
        ENTERTAINMENT_GENERATOR("MISTRAL", MISTRAL_MODEL),
        GROWTH_STRATEGIST("MISTRAL", MISTRAL_MODEL),
        FALLBACK("PHI", PHI_MODEL);

        private final String shortName;
        private final String modelName;

        ModelType(String shortName, String modelName) {
            this.shortName = shortName;
            this.modelName = modelName;
        }

        public String getModelName() {
            return modelName;
        }

        public String getShortName() {
            return shortName;
        }
    }

    public String selectModel(String contentType) {
        log.info("Routing model selection for content type: {}", contentType);

        if ("educational".equalsIgnoreCase(contentType)) {
            return selectBestHealthyModel(LLAMA_MODEL, MISTRAL_MODEL);
        } else if ("entertainment".equalsIgnoreCase(contentType)) {
            return selectBestHealthyModel(MISTRAL_MODEL, LLAMA_MODEL);
        }

        return MISTRAL_MODEL;
    }

    public String getFallbackModel(String primaryModel) {
        log.info("Selecting fallback model for failed primary: {}", primaryModel);

        if (LLAMA_MODEL.equals(primaryModel)) {
            return MISTRAL_MODEL;
        } else if (MISTRAL_MODEL.equals(primaryModel)) {
            return LLAMA_MODEL;
        }

        return PHI_MODEL;
    }

    private String selectBestHealthyModel(String preferredModel, String fallbackModel) {
        ModelPerformanceLog preferredPerf = modelPerformanceLogRepository.findByModelName(preferredModel)
            .orElse(null);

        if (preferredPerf != null && preferredPerf.getIsHealthy() && preferredPerf.getReliabilityScore().compareTo(BigDecimal.valueOf(0.7)) > 0) {
            log.info("Selected primary model: {} with reliability score: {}", preferredModel, preferredPerf.getReliabilityScore());
            return preferredModel;
        }

        log.info("Primary model {} not healthy or low reliability, using fallback: {}", preferredModel, fallbackModel);
        return fallbackModel;
    }

    public void logModelUsage(String modelName, long latencyMs, Integer tokenUsed, boolean success) {
        ModelPerformanceLog modelLog = modelPerformanceLogRepository.findByModelName(modelName)
            .orElse(ModelPerformanceLog.builder()
                .modelName(modelName)
                .requestCount(0)
                .successCount(0)
                .failureCount(0)
                .build()
            );

        modelLog.setRequestCount(modelLog.getRequestCount() + 1);
        if (success) {
            modelLog.setSuccessCount(modelLog.getSuccessCount() + 1);
        } else {
            modelLog.setFailureCount(modelLog.getFailureCount() + 1);
        }

        Integer totalLatency = (modelLog.getAverageLatencyMs() != null ? modelLog.getAverageLatencyMs() : 0);
        Integer newAvgLatency = (int) ((totalLatency * (modelLog.getRequestCount() - 1) + latencyMs) / modelLog.getRequestCount());
        modelLog.setAverageLatencyMs(newAvgLatency);

        if (tokenUsed != null) {
            Integer totalTokens = (modelLog.getAverageTokensUsed() != null ? modelLog.getAverageTokensUsed() : 0);
            Integer newAvgTokens = (totalTokens * (modelLog.getRequestCount() - 1) + tokenUsed) / modelLog.getRequestCount();
            modelLog.setAverageTokensUsed(newAvgTokens);
        }

        modelLog.setLastUsed(LocalDateTime.now());

        if (modelLog.getRequestCount() > 0) {
            BigDecimal reliabilityScore = BigDecimal.valueOf((double) modelLog.getSuccessCount() / modelLog.getRequestCount());
            modelLog.setReliabilityScore(reliabilityScore);
            modelLog.setIsHealthy(reliabilityScore.compareTo(BigDecimal.valueOf(0.7)) > 0);
        }

        modelLog.setUpdatedAt(LocalDateTime.now());
        modelPerformanceLogRepository.save(modelLog);

        log.debug("Logged usage for model {} - Success: {}, Latency: {}ms, Reliability: {}",
            modelName, success, latencyMs, modelLog.getReliabilityScore());
    }

    public void reportModelFailure(String modelName, String errorMessage) {
        log.error("Model {} reported as failed: {}", modelName, errorMessage);
        ModelPerformanceLog log = modelPerformanceLogRepository.findByModelName(modelName)
            .orElse(ModelPerformanceLog.builder()
                .modelName(modelName)
                .isHealthy(false)
                .build()
            );

        log.setIsHealthy(false);
        log.setUpdatedAt(LocalDateTime.now());
        modelPerformanceLogRepository.save(log);
    }

    public List<ModelPerformanceLog> getHealthyModels() {
        return modelPerformanceLogRepository.findByIsHealthyTrue();
    }

    public List<ModelPerformanceLog> getModelPerformanceRanking() {
        return modelPerformanceLogRepository.findAllByOrderByReliabilityScoreDesc();
    }
}
