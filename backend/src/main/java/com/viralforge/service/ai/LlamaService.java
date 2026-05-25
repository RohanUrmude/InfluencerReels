package com.viralforge.service.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.time.Duration;

@Slf4j
@Service
public class LlamaService {
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    @Value("${huggingface.api.url:https://router.huggingface.co/v1/chat/completions}")
    private String huggingFaceApiUrl;

    private static final String MODEL_NAME = "meta-llama/Meta-Llama-3-8B-Instruct:novita";
    private static final long TIMEOUT_MS = 120000;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    public LlamaService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .setReadTimeout(Duration.ofMillis(TIMEOUT_MS))
            .build();
    }

    public String generateEducationalScript(
        String topic,
        String targetAudience,
        String platform,
        String vibe,
        String creatorGoal
    ) throws AIServiceException {
        long startTime = System.currentTimeMillis();

        try {
            String prompt = buildEducationalScriptPrompt(topic, targetAudience, platform, vibe, creatorGoal);
            String response = callHuggingFaceAPI(prompt);
            String script = extractScriptContent(response);

            long latencyMs = System.currentTimeMillis() - startTime;
            log.info("Llama educational script generation completed in {}ms", latencyMs);

            return script;
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in Llama service script generation", e);
            throw new AIServiceException("Educational script generation failed: " + e.getMessage(), e);
        }
    }

    private String buildEducationalScriptPrompt(
        String topic,
        String targetAudience,
        String platform,
        String vibe,
        String creatorGoal
    ) {
        topic = PromptSanitizer.sanitize(topic);
        targetAudience = PromptSanitizer.sanitize(targetAudience);
        platform = PromptSanitizer.sanitize(platform);
        vibe = PromptSanitizer.sanitize(vibe);
        creatorGoal = PromptSanitizer.sanitize(creatorGoal);

        return String.format("""
            Create a viral educational short-form social media script:

            Topic: %s
            Platform: %s
            Target Audience: %s
            Vibe: %s
            Goal: %s

            SCRIPT:
            [Hook - 3 seconds max, grab attention immediately]

            [Main Content - Educational value, smart explanation, 10 seconds]

            [Retention - Why they should watch till end, 5 seconds]

            [CTA - Clear call to action, follow/like/share, 2 seconds]

            [Carousel Idea - 5 slide carousel concept for this topic]

            [Storytelling Flow - How to maintain engagement throughout]

            Make it creator-style, authentic, engaging, and optimized for %s platform.
            Keep language Gen Z friendly but professional enough to educate.
            """, topic, platform, targetAudience, vibe, creatorGoal, platform);
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

    private String extractScriptContent(String response) {
        try {
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            if (jsonResponse.has("choices") && jsonResponse.get("choices").isJsonArray()) {
                com.google.gson.JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    if (choice.has("message")) {
                        JsonObject message = choice.getAsJsonObject("message");
                        if (message.has("content")) {
                            return message.get("content").getAsString();
                        }
                    }
                }
            }

            return response;
        } catch (Exception e) {
            log.warn("Failed to parse response, returning raw response", e);
            return response;
        }
    }
}
