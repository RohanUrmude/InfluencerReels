package com.viralforge.service.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Interceptor to log and validate model responses without changing core logic
 * Can be injected into services to track performance and validate outputs
 */
@Slf4j
@Component
public class ModelResponseInterceptor {
    @Autowired(required = false)
    private ModelValidationService validationService;

    private boolean validationEnabled = true;

    /**
     * Log and validate a model response
     */
    public void interceptResponse(String modelName, String prompt, String responseContent, Integer tokensUsed) {
        if (!validationEnabled || validationService == null) {
            log.debug("Validation disabled or service not available");
            return;
        }

        try {
            // Start tracking
            validationService.startModelTracking(modelName);

            // Record response
            validationService.recordModelResponse(modelName, responseContent, tokensUsed);

            // Validate with Claude (async-friendly)
            new Thread(() -> {
                try {
                    validationService.validateResponseWithClaude(modelName, prompt, responseContent);
                    validationService.scoreModelResponse(modelName, responseContent, "Model output quality assessment");
                    validationService.savePerformanceMetrics(modelName);
                    log.info("Model {} validation and metrics saved successfully", modelName);
                } catch (Exception e) {
                    log.error("Error in async validation for {}: {}", modelName, e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            log.error("Error intercepting model response for {}: {}", modelName, e.getMessage());
            // Don't fail the main request if validation fails
        }
    }

    /**
     * Batch validate multiple model responses
     */
    public void batchValidateResponses(String prompt, java.util.Map<String, String> modelResponses) {
        if (!validationEnabled || validationService == null) {
            return;
        }

        try {
            new Thread(() -> {
                try {
                    validationService.compareModelResponses(modelResponses, prompt);
                    log.info("Batch validation completed for {} models", modelResponses.size());
                } catch (Exception e) {
                    log.error("Error in batch validation: {}", e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            log.error("Error starting batch validation: {}", e.getMessage());
        }
    }

    /**
     * Enable/disable validation
     */
    public void setValidationEnabled(boolean enabled) {
        this.validationEnabled = enabled;
        log.info("Model validation {}", enabled ? "enabled" : "disabled");
    }

    /**
     * Get validation status
     */
    public boolean isValidationEnabled() {
        return validationEnabled && validationService != null;
    }
}
