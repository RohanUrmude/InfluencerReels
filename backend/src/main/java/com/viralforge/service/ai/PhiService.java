package com.viralforge.service.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.viralforge.dto.response.AudienceAnalysisDTO;
import com.viralforge.exception.AIServiceException;
import com.viralforge.util.PromptSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class PhiService {
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    @Value("${huggingface.api.url:https://router.huggingface.co/v1/chat/completions}")
    private String huggingFaceApiUrl;

    private static final String MODEL_NAME = "microsoft/Phi-3-mini-4k-instruct:featherless-ai";
    private static final long TIMEOUT_MS = 120000;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    public PhiService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .setReadTimeout(Duration.ofMillis(TIMEOUT_MS))
            .build();
    }

    public AudienceAnalysisDTO analyzeAudience(
        String niche,
        String vibe,
        String topic,
        String audience,
        String platform
    ) throws AIServiceException {
        long startTime = System.currentTimeMillis();

        try {
            String prompt = buildAudienceAnalysisPrompt(niche, vibe, topic, audience, platform);
            String response = callHuggingFaceAPI(prompt);
            AudienceAnalysisDTO analysis = parseAudienceAnalysis(response);

            long latencyMs = System.currentTimeMillis() - startTime;
            log.info("Phi service audience analysis completed in {}ms for niche: {}", latencyMs, niche);

            return analysis;
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in Phi service audience analysis", e);
            throw new AIServiceException("Audience analysis failed: " + e.getMessage(), e);
        }
    }

    private String buildAudienceAnalysisPrompt(
        String niche,
        String vibe,
        String topic,
        String audience,
        String platform
    ) {
        niche = PromptSanitizer.sanitize(niche);
        vibe = PromptSanitizer.sanitize(vibe);
        topic = PromptSanitizer.sanitize(topic);
        audience = PromptSanitizer.sanitize(audience);
        platform = PromptSanitizer.sanitize(platform);

        return String.format("""
            You are an expert social media analyst specializing in viral content prediction. Analyze this creator content idea and provide detailed insights for viral potential.

            CONTENT DETAILS:
            Niche: %s
            Content Vibe: %s
            Topic/Idea: %s
            Target Audience: %s
            Platform: %s

            ANALYSIS TASK:
            1. Evaluate viral potential (0-10 scale): Consider trending topics, audience alignment, hook strength, shareability
            2. Assess confidence (0-100): How certain are you about this prediction? High confidence if: topic is trending, audience is clear, platform-niche fit is strong
            3. Identify engagement triggers that resonate with the audience
            4. Suggest platform-specific hashtags that match current trends
            5. Evaluate alignment with current social media trends
            6. Identify viral hooks that will stop the scroll

            SCORING GUIDELINES:
            - High Confidence (80-100): Trending topic, clear audience, strong platform fit, similar successful content exists
            - Medium Confidence (50-79): Decent concept, some market validation, minor gaps
            - Low Confidence (20-49): Niche topic, unclear audience, weak platform fit

            Respond with ONLY valid JSON, no markdown:
            {
              "audienceType": "describe who watches this content",
              "viralPotential": 5.5,
              "confidenceScore": 85.0,
              "recommendedTone": "tone recommendation",
              "contentStyle": "visual/storytelling style",
              "engagementTriggers": ["psychological trigger 1", "emotional trigger 2"],
              "hashtags": ["#trend1", "#trend2", "#trend3"],
              "trendAlignment": "how it aligns with current trends",
              "viralHooks": ["hook that stops the scroll", "hook with curiosity gap"],
              "recommendedCTA": "specific call-to-action"
            }
            """, niche, vibe, topic, audience, platform);
    }

    private String callHuggingFaceAPI(String prompt) throws AIServiceException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + huggingFaceApiKey);

            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("role", "user");
            messageObj.addProperty("content", prompt);

            com.google.gson.JsonArray messagesArray = new com.google.gson.JsonArray();
            messagesArray.add(messageObj);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL_NAME);
            requestBody.add("messages", messagesArray);
            requestBody.addProperty("max_tokens", 1024);
            requestBody.addProperty("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            log.debug("Calling Hugging Face Router API with model: {}", MODEL_NAME);
            String response = restTemplate.postForObject(
                huggingFaceApiUrl,
                request,
                String.class
            );

            if (response == null || response.isEmpty()) {
                throw new AIServiceException("Empty response from Hugging Face API");
            }

            log.debug("Hugging Face API response received successfully");
            return response;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.error("Rate limited by Hugging Face API (429). Retrying...", e);
                throw new AIServiceException("Rate limited by Hugging Face API. Please try again later.", e);
            } else if (e.getStatusCode().value() == 503) {
                log.error("Hugging Face API service unavailable (503). Retrying...", e);
                throw new AIServiceException("Hugging Face API temporarily unavailable. Please try again.", e);
            }
            log.error("HTTP error from Hugging Face API: {} {}", e.getStatusCode(), e.getStatusText());
            throw new AIServiceException("HTTP error from Hugging Face API: " + e.getStatusCode() + " - " + e.getStatusText(), e);
        } catch (ResourceAccessException e) {
            log.error("Timeout or network error calling Hugging Face API", e);
            throw new AIServiceException("Connection timeout with Hugging Face API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Hugging Face API call failed", e);
            throw new AIServiceException("Failed to call Hugging Face API: " + e.getMessage(), e);
        }
    }

    private AudienceAnalysisDTO parseAudienceAnalysis(String response) throws AIServiceException {
        try {
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            String contentText = "";

            if (jsonResponse.has("choices") && jsonResponse.get("choices").isJsonArray()) {
                com.google.gson.JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    if (choice.has("message")) {
                        JsonObject message = choice.getAsJsonObject("message");
                        if (message.has("content")) {
                            contentText = message.get("content").getAsString();
                        }
                    }
                }
            }

            String jsonPart = extractJsonFromText(contentText);
            jsonResponse = gson.fromJson(jsonPart, JsonObject.class);

            return AudienceAnalysisDTO.builder()
                .audienceType(getString(jsonResponse, "audienceType"))
                .viralPotential(getDecimal(jsonResponse, "viralPotential"))
                .confidenceScore(getDecimal(jsonResponse, "confidenceScore"))
                .recommendedTone(getString(jsonResponse, "recommendedTone"))
                .contentStyle(getString(jsonResponse, "contentStyle"))
                .engagementTriggers(getStringList(jsonResponse, "engagementTriggers"))
                .hashtags(getStringList(jsonResponse, "hashtags"))
                .trendAlignment(getString(jsonResponse, "trendAlignment"))
                .viralHooks(getStringList(jsonResponse, "viralHooks"))
                .recommendedCta(getString(jsonResponse, "recommendedCTA"))
                .build();
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse JSON response from Phi service", e);
            throw new AIServiceException("Invalid JSON response from AI model: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromText(String text) throws AIServiceException {
        int startIdx = text.indexOf('{');
        int endIdx = text.lastIndexOf('}');

        if (startIdx == -1 || endIdx == -1 || startIdx >= endIdx) {
            throw new AIServiceException("Could not find valid JSON in response: " + text.substring(0, Math.min(100, text.length())));
        }

        return text.substring(startIdx, endIdx + 1);
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull()
            ? obj.get(key).getAsString()
            : "";
    }

    private BigDecimal getDecimal(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return new BigDecimal(obj.get(key).getAsString());
        }
        return BigDecimal.ZERO;
    }

    private List<String> getStringList(JsonObject obj, String key) {
        if (obj.has(key) && obj.get(key).isJsonArray()) {
            return Arrays.asList(gson.fromJson(obj.get(key), String[].class));
        }
        return List.of();
    }
}
