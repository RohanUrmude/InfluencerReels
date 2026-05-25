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
public class MistralService {
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    @Value("${huggingface.api.url:https://router.huggingface.co/v1/chat/completions}")
    private String huggingFaceApiUrl;

    private static final String MODEL_NAME = "mistralai/Mistral-7B-Instruct-v0.2:featherless-ai";
    private static final long TIMEOUT_MS = 120000;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    public MistralService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .setReadTimeout(Duration.ofMillis(TIMEOUT_MS))
            .build();
    }

    public String generateViralEntertainmentScript(
        String topic,
        String targetAudience,
        String platform,
        String vibe,
        String creatorGoal
    ) throws AIServiceException {
        long startTime = System.currentTimeMillis();

        try {
            String prompt = buildEntertainmentScriptPrompt(topic, targetAudience, platform, vibe, creatorGoal);
            String response = callHuggingFaceAPI(prompt);
            String script = extractScriptContent(response);

            long latencyMs = System.currentTimeMillis() - startTime;
            log.info("Mistral entertainment script generation completed in {}ms", latencyMs);

            return script;
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in Mistral service script generation", e);
            throw new AIServiceException("Entertainment script generation failed: " + e.getMessage(), e);
        }
    }

    public String generateGrowthStrategy(
        String niche,
        String platform,
        String topicIdea,
        String contentType
    ) throws AIServiceException {
        long startTime = System.currentTimeMillis();

        try {
            String prompt = buildGrowthStrategyPrompt(niche, platform, topicIdea, contentType);
            String response = callHuggingFaceAPI(prompt);
            String strategy = extractStrategyContent(response);

            long latencyMs = System.currentTimeMillis() - startTime;
            log.info("Mistral growth strategy generation completed in {}ms", latencyMs);

            return strategy;
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in Mistral service growth strategy generation", e);
            throw new AIServiceException("Growth strategy generation failed: " + e.getMessage(), e);
        }
    }

    private String buildEntertainmentScriptPrompt(
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
            Create a VIRAL entertainment short-form script for %s:

            Topic: %s
            Audience: %s (vibe: %s)
            Goal: %s

            VIRAL ENTERTAINMENT SCRIPT:
            [HOOK - Emotional grab, surprising statement, or meme-style joke (3 seconds)]

            [ENGAGEMENT BAIT - Why they MUST watch to the end (2 seconds)]

            [HUMOR/ENTERTAINMENT - Meme references, trending phrases, creator energy (8 seconds)]

            [RETENTION - Keep watching factor, unexpected twist (3 seconds)]

            [EMOTIONAL PAYOFF - Satisfying ending or punchline (2 seconds)]

            [CTA - Call to action: like, share, follow, comment (2 seconds)]

            Include:
            - 3-5 trending TikTok/Instagram meme phrases
            - Emotional hooks that resonate with Gen Z
            - Storytelling beats for maximum watch time
            - Why this will go viral
            - Suggested hashtags for the content

            Make it authentic, funny, relatable, and optimized for viral spread on %s.
            Use Gen Z language and current trends.
            """, platform, topic, targetAudience, vibe, creatorGoal, platform);
    }

    private String buildGrowthStrategyPrompt(
        String niche,
        String platform,
        String topicIdea,
        String contentType
    ) {
        niche = PromptSanitizer.sanitize(niche);
        platform = PromptSanitizer.sanitize(platform);
        topicIdea = PromptSanitizer.sanitize(topicIdea);
        contentType = PromptSanitizer.sanitize(contentType);

        return String.format("""
            You are a social media growth strategist. Create a detailed, actionable growth strategy for this content. Respond with ONLY valid JSON, no markdown:

            Niche: %s
            Platform: %s
            Content Type: %s
            Topic: %s

            {
              "bestPostingTime": "specific days and times for maximum reach (e.g., Tuesday-Thursday 4-6 PM EST)",
              "postingSchedule": "recommended posting frequency (e.g., 3-5 times per week)",
              "seoHashtags": ["#hashtag1", "#hashtag2", "#hashtag3", "#hashtag4", "#hashtag5"],
              "thumbnailText": "optimal text overlay for thumbnail if applicable",
              "captionHook": "attention-grabbing opening line for caption",
              "engagementTriggers": ["trigger1", "trigger2", "trigger3"],
              "engagementStrategy": "specific tactics to engage audience in comments and DMs",
              "platformOptimization": "platform-specific optimization tips (transitions, effects, sounds)",
              "crossPlatformStrategy": "how to adapt this content for other platforms",
              "collaborationOpportunities": "who to collaborate with and how",
              "seriesIdea": "ideas for creating a series to boost watch time",
              "trendingAudio": "recommended trending sounds/music",
              "analyticsToTrack": ["metric1", "metric2", "metric3"],
              "growthHacks": ["hack1", "hack2", "hack3"]
            }
            """, niche, platform, contentType, topicIdea);
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

    private String extractStrategyContent(String response) {
        return extractScriptContent(response);
    }
}
