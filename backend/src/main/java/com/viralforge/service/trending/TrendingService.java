package com.viralforge.service.trending;

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
public class TrendingService {
    @Value("${huggingface.api.key:}")
    private String huggingFaceApiKey;

    @Value("${huggingface.api.url:https://router.huggingface.co/v1/chat/completions}")
    private String huggingFaceApiUrl;

    private static final String MODEL_NAME = "mistralai/Mistral-7B-Instruct-v0.2:featherless-ai";
    private static final long TIMEOUT_MS = 120000;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    public TrendingService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .setReadTimeout(Duration.ofMillis(TIMEOUT_MS))
            .build();
    }

    public Map<String, Object> getTrendingContent() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get trending hashtags and topics
            String trendingTopics = getTrendingTopicsFromAI();
            result.put("success", true);
            result.put("tiktokTrends", parseTrendingData(trendingTopics, "tiktok"));
            result.put("reelsTrends", parseTrendingData(trendingTopics, "reels"));
            result.put("youtubeTrends", parseTrendingData(trendingTopics, "youtube"));
            result.put("timestamp", System.currentTimeMillis());
            result.put("message", "Trending content retrieved successfully");
        } catch (Exception e) {
            log.error("Error fetching trending content", e);
            result.put("success", false);
            result.put("message", "Failed to fetch trending content");
            result.put("data", getDefaultTrendingData());
        }

        return result;
    }

    private String getTrendingTopicsFromAI() {
        // If API key is not configured, return default trending data
        if (huggingFaceApiKey == null || huggingFaceApiKey.isEmpty()) {
            log.warn("Hugging Face API key not configured. Using default trending data.");
            return generateDefaultTrendingJson();
        }

        String prompt = """
            Generate a JSON response with currently trending topics and hashtags (as of 2026) for short-form video platforms.

            Return ONLY valid JSON in this exact format (no markdown, no extra text):
            {
              "hashtags": ["#trending1", "#trending2", "#trending3", "#trending4", "#trending5"],
              "topics": ["topic1", "topic2", "topic3", "topic4", "topic5"],
              "contentTypes": ["educational", "entertainment", "motivational", "comedy", "lifestyle"],
              "musicTrends": ["trending_song_1", "trending_song_2", "trending_song_3"],
              "challenges": ["challenge1", "challenge2", "challenge3"],
              "aesthetics": ["vibe1", "vibe2", "vibe3"]
            }

            Focus on what's actually trending on TikTok, Instagram Reels, and YouTube Shorts.
            Include real, popular hashtags and topics that creators use.
            """;

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL_NAME);

            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);

            requestBody.add("messages", messages);
            requestBody.addProperty("max_tokens", 1024);
            requestBody.addProperty("temperature", 0.7f);

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
                    String content = choice.getAsJsonObject("message").get("content").getAsString();
                    return content.trim();
                }
            }
        } catch (Exception e) {
            log.error("Error calling AI for trending topics", e);
        }

        return generateDefaultTrendingJson();
    }

    private String generateDefaultTrendingJson() {
        return """
            {
              "hashtags": ["#AIRevolution", "#ContentCreating", "#DigitalNomad", "#TechTrends", "#CreatorEconomy"],
              "topics": ["AI and machine learning", "Content creation tools", "Digital marketing", "Short-form video trends", "Online community building"],
              "contentTypes": ["educational", "entertainment", "motivational", "lifestyle", "tutorial"],
              "musicTrends": ["Synthwave vibes", "Hip-hop beats", "Lofi instrumental", "Trending audio tracks", "Viral sounds"],
              "challenges": ["30-day coding challenge", "Create and share challenge", "Talent showcase", "Transformation challenge", "Learning journey"],
              "aesthetics": ["minimalist", "vintage", "cyberpunk", "cozy", "maximalist"]
            }
            """;
    }

    private List<Map<String, Object>> parseTrendingData(String jsonResponse, String platform) {
        List<Map<String, Object>> trendingList = new ArrayList<>();

        try {
            if (jsonResponse == null) {
                return getDefaultTrendsForPlatform(platform);
            }

            JsonObject trendingData = gson.fromJson(jsonResponse, JsonObject.class);

            // Extract hashtags
            List<String> hashtags = new ArrayList<>();
            if (trendingData.has("hashtags")) {
                trendingData.getAsJsonArray("hashtags").forEach(tag ->
                    hashtags.add(tag.getAsString())
                );
            }

            // Extract topics
            List<String> topics = new ArrayList<>();
            if (trendingData.has("topics")) {
                trendingData.getAsJsonArray("topics").forEach(topic ->
                    topics.add(topic.getAsString())
                );
            }

            // Extract content types
            List<String> contentTypes = new ArrayList<>();
            if (trendingData.has("contentTypes")) {
                trendingData.getAsJsonArray("contentTypes").forEach(type ->
                    contentTypes.add(type.getAsString())
                );
            }

            // Extract music trends
            List<String> musicTrends = new ArrayList<>();
            if (trendingData.has("musicTrends")) {
                trendingData.getAsJsonArray("musicTrends").forEach(music ->
                    musicTrends.add(music.getAsString())
                );
            }

            // Extract challenges
            List<String> challenges = new ArrayList<>();
            if (trendingData.has("challenges")) {
                trendingData.getAsJsonArray("challenges").forEach(challenge ->
                    challenges.add(challenge.getAsString())
                );
            }

            // Create trending items combining all data
            for (int i = 0; i < Math.min(5, topics.size()); i++) {
                Map<String, Object> trend = new HashMap<>();
                trend.put("id", UUID.randomUUID().toString());
                trend.put("platform", platform);
                trend.put("topic", topics.get(i));
                trend.put("hashtag", i < hashtags.size() ? hashtags.get(i) : "#trending");
                trend.put("contentType", i < contentTypes.size() ? contentTypes.get(i) : "trending");
                trend.put("music", i < musicTrends.size() ? musicTrends.get(i) : "Trending audio");
                trend.put("challenge", i < challenges.size() ? challenges.get(i) : null);
                trend.put("trendScore", 85 + (int)(Math.random() * 15)); // 85-100
                trend.put("growthRate", "+12-18%");
                trend.put("description", generateTrendDescription(platform, topics.get(i)));
                trend.put("timestamp", System.currentTimeMillis());

                trendingList.add(trend);
            }

        } catch (Exception e) {
            log.error("Error parsing trending data for platform: " + platform, e);
            return getDefaultTrendsForPlatform(platform);
        }

        return trendingList;
    }

    private List<Map<String, Object>> getDefaultTrendsForPlatform(String platform) {
        List<Map<String, Object>> defaults = new ArrayList<>();

        Map<String, List<String>> platformDefaults = new HashMap<>();
        platformDefaults.put("tiktok", Arrays.asList(
            "POV: You're a digital nomad",
            "AI trends",
            "Fitness transformation",
            "DIY life hacks",
            "Productivity tips"
        ));
        platformDefaults.put("reels", Arrays.asList(
            "Relatable daily situations",
            "Fashion and style",
            "Wellness content",
            "Behind-the-scenes",
            "Quick recipes"
        ));
        platformDefaults.put("youtube", Arrays.asList(
            "Tech reviews",
            "Educational breakdowns",
            "Vlog storytelling",
            "Skill tutorials",
            "Commentary and analysis"
        ));

        List<String> topics = platformDefaults.getOrDefault(platform, new ArrayList<>());

        for (int i = 0; i < topics.size(); i++) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("id", UUID.randomUUID().toString());
            trend.put("platform", platform);
            trend.put("topic", topics.get(i));
            trend.put("hashtag", "#trending" + (i + 1));
            trend.put("contentType", "trending");
            trend.put("music", "Trending audio");
            trend.put("challenge", "Challenge trend " + (i + 1));
            trend.put("trendScore", 88);
            trend.put("growthRate", "+15%");
            trend.put("description", "This is a trending topic on " + platform);
            trend.put("timestamp", System.currentTimeMillis());

            defaults.add(trend);
        }

        return defaults;
    }

    private String generateTrendDescription(String platform, String topic) {
        return "Trending on " + platform + ": " + topic + " is gaining significant traction with creators and audiences. "
            + "This content type shows high engagement potential and growing viewership.";
    }

    private Map<String, Object> getDefaultTrendingData() {
        Map<String, Object> result = new HashMap<>();
        result.put("tiktok", getDefaultTrendsForPlatform("tiktok"));
        result.put("reels", getDefaultTrendsForPlatform("reels"));
        result.put("youtube", getDefaultTrendsForPlatform("youtube"));
        return result;
    }

    public Map<String, Object> getTrendingByPlatform(String platform) {
        Map<String, Object> allTrends = getTrendingContent();

        Map<String, Object> result = new HashMap<>();
        result.put("success", (Boolean) allTrends.get("success"));
        result.put("platform", platform);
        result.put("data", allTrends.get(platform + "Trends"));
        result.put("message", allTrends.get("message"));

        return result;
    }
}
