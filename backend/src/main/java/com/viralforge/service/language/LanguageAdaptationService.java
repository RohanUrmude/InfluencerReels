package com.viralforge.service.language;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LanguageAdaptationService {
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    @Value("${huggingface.api.url:https://router.huggingface.co/v1/chat/completions}")
    private String huggingFaceApiUrl;

    private static final String MODEL_NAME = "mistralai/Mistral-7B-Instruct-v0.2:featherless-ai";
    private static final long TIMEOUT_MS = 120000;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    private static final Map<String, String> INDIAN_LANGUAGES = new LinkedHashMap<>();

    static {
        INDIAN_LANGUAGES.put("hi", "Hindi (हिन्दी)");
        INDIAN_LANGUAGES.put("bn", "Bengali (বাংলা)");
        INDIAN_LANGUAGES.put("te", "Telugu (తెలుగు)");
        INDIAN_LANGUAGES.put("mr", "Marathi (मराठी)");
        INDIAN_LANGUAGES.put("ta", "Tamil (தமிழ்)");
        INDIAN_LANGUAGES.put("gu", "Gujarati (ગુજરાતી)");
        INDIAN_LANGUAGES.put("ur", "Urdu (اردو)");
        INDIAN_LANGUAGES.put("kn", "Kannada (ಕನ್ನಡ)");
        INDIAN_LANGUAGES.put("pa", "Punjabi (ਪੰਜਾਬੀ)");
        INDIAN_LANGUAGES.put("ml", "Malayalam (മലയാളം)");
    }

    public LanguageAdaptationService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .setReadTimeout(Duration.ofMillis(TIMEOUT_MS))
            .build();
    }

    public Map<String, Object> adaptContentToLanguages(String originalContent, String topic, String platform, String niche, String targetAudience, List<String> languageCodes) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> translations = new HashMap<>();

        // Add original content in English
        translations.put("en", originalContent);

        // Get valid language codes
        List<String> validLanguages = languageCodes.stream()
            .filter(INDIAN_LANGUAGES::containsKey)
            .collect(Collectors.toList());

        if (validLanguages.isEmpty()) {
            validLanguages = new ArrayList<>(INDIAN_LANGUAGES.keySet());
        }

        // Translate to each Indian language
        for (String langCode : validLanguages) {
            try {
                String translation = translateContent(originalContent, langCode, topic, niche, targetAudience, platform);
                translations.put(langCode, translation);
                log.info("Successfully translated content to {}", INDIAN_LANGUAGES.get(langCode));
            } catch (Exception e) {
                log.error("Error translating to {}: {}", langCode, e.getMessage());
                // Fallback to original content if translation fails
                translations.put(langCode, originalContent);
            }
        }

        result.put("success", true);
        result.put("languages", INDIAN_LANGUAGES);
        result.put("translations", translations);
        result.put("message", "Content adapted to Indian languages successfully");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    private String translateContent(String content, String languageCode, String topic, String niche, String targetAudience, String platform) {
        String languageName = INDIAN_LANGUAGES.get(languageCode);

        String prompt = String.format("""
            You are an expert content translator and cultural adaptation specialist for Indian languages.

            Translate the following viral content script to %s (%s).

            IMPORTANT REQUIREMENTS:
            1. Translate accurately but use NATURAL, NATIVE SPEAKER style language
            2. Adapt cultural references and humor for %s speakers
            3. Keep the viral hooks and engagement triggers intact
            4. Use region-specific expressions and slang where appropriate
            5. Maintain the same tone and energy as the original
            6. If there are specific cultural nuances for this region, incorporate them

            CONTEXT:
            - Platform: %s
            - Niche: %s
            - Target Audience: %s
            - Original Niche/Region: India (%s)

            ORIGINAL CONTENT:
            %s

            TRANSLATED CONTENT IN %s (NATIVE STYLE):
            """, languageName, languageCode, languageName, platform, niche, targetAudience, languageName, content, languageName);

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL_NAME);

            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);

            requestBody.add("messages", messages);
            requestBody.addProperty("max_tokens", 1500);
            requestBody.addProperty("temperature", 0.8f);

            String requestJson = gson.toJson(requestBody);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingFaceApiKey);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestJson, headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                huggingFaceApiUrl,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonObject responseBody = gson.fromJson(response.getBody(), JsonObject.class);
                if (responseBody.has("choices") && responseBody.getAsJsonArray("choices").size() > 0) {
                    JsonObject choice = responseBody.getAsJsonArray("choices").get(0).getAsJsonObject();
                    String translatedContent = choice.getAsJsonObject("message").get("content").getAsString();
                    return translatedContent.trim();
                }
            }

            return content;
        } catch (Exception e) {
            log.error("Error in translation API call for {}: {}", languageCode, e.getMessage());
            return content;
        }
    }

    public Map<String, String> getAvailableLanguages() {
        return new LinkedHashMap<>(INDIAN_LANGUAGES);
    }

    public String generateSubtitles(String content, String languageCode) {
        String languageName = INDIAN_LANGUAGES.getOrDefault(languageCode, "English");

        String prompt = String.format("""
            Generate SRT subtitle format for the following content in %s.

            CONTENT:
            %s

            Generate subtitles with proper timing (00:00:00,000 --> 00:00:05,000 format).
            Each subtitle should be 1-2 lines maximum.
            Make subtitles natural and easy to read.

            Return ONLY the SRT format with no additional text.
            """, languageName, content);

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL_NAME);

            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);

            requestBody.add("messages", messages);
            requestBody.addProperty("max_tokens", 1000);
            requestBody.addProperty("temperature", 0.5f);

            String requestJson = gson.toJson(requestBody);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingFaceApiKey);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestJson, headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                huggingFaceApiUrl,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonObject responseBody = gson.fromJson(response.getBody(), JsonObject.class);
                if (responseBody.has("choices") && responseBody.getAsJsonArray("choices").size() > 0) {
                    JsonObject choice = responseBody.getAsJsonArray("choices").get(0).getAsJsonObject();
                    String subtitles = choice.getAsJsonObject("message").get("content").getAsString();
                    return subtitles.trim();
                }
            }

            return "";
        } catch (Exception e) {
            log.error("Error generating subtitles for {}: {}", languageCode, e.getMessage());
            return "";
        }
    }
}
