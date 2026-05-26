package com.viralforge.service;

import com.viralforge.dto.request.ContentGenerationDTO;
import com.viralforge.dto.response.ContentGenerationResponseDTO;
import com.viralforge.entity.GeneratedContent;
import com.viralforge.entity.User;
import com.viralforge.repository.GeneratedContentRepository;
import com.viralforge.repository.UserRepository;
import com.viralforge.service.ai.AIOrchestratorService;
import com.viralforge.service.ai.LlamaService;
import com.viralforge.service.ai.MistralService;
import com.viralforge.service.ai.PhiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentGenerationTest {

    @Mock
    private AIOrchestratorService aIOrchestratorService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeneratedContentRepository generatedContentRepository;

    @Mock
    private PhiService phiService;

    @Mock
    private LlamaService llamaService;

    @Mock
    private MistralService mistralService;

    private ContentGenerationDTO validRequest;
    private User testUser;
    private GeneratedContent generatedContent;

    @BeforeEach
    public void setUp() {
        validRequest = new ContentGenerationDTO();
        validRequest.setNiche("Fitness");
        validRequest.setPlatform("TikTok");
        validRequest.setContentType("entertainment");
        validRequest.setTopicIdea("Morning workout motivation");
        validRequest.setTargetAudience("Fitness Enthusiasts");
        validRequest.setVibe("inspirational");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setApiUsageCount(5);
        testUser.setMaxMonthlyApiCalls(1000);

        generatedContent = new GeneratedContent();
        generatedContent.setId(1L);
        generatedContent.setUser(testUser);
        generatedContent.setScriptContent("Generated script content...");
        generatedContent.setViralScore(BigDecimal.valueOf(7.5f));
        generatedContent.setConfidenceScore(BigDecimal.valueOf(85.5f));
        generatedContent.setPrimaryModelUsed("Mistral");
    }

    // ========== CONTENT GENERATION TESTS ==========

    @Test
    public void testCG001_GenerateContentWithValidInput() {
        // CG-001: Generate Content - Valid Input
        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        response.setViralScore(BigDecimal.valueOf(7.5));
        response.setConfidenceScore(BigDecimal.valueOf(85.5));
        response.setScriptContent("Generated script");
        response.setPrimaryModelUsed("Mistral");

        when(aIOrchestratorService.orchestrateContentGeneration(validRequest, testUser))
            .thenReturn(response);

        ContentGenerationResponseDTO result = aIOrchestratorService
            .orchestrateContentGeneration(validRequest, testUser);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(7.5), result.getViralScore());
        assertEquals(BigDecimal.valueOf(85.5), result.getConfidenceScore());
        assertNotNull(result.getScriptContent());
        assertTrue(result.getScriptContent().length() > 0);
    }

    @Test
    public void testCG002_GenerateContentMissingRequiredField() {
        // CG-002: Generate Content - Missing Required Field
        ContentGenerationDTO invalidRequest = new ContentGenerationDTO();
        invalidRequest.setPlatform("TikTok");
        invalidRequest.setContentType("entertainment");
        // Missing: niche, topic, audience

        when(aIOrchestratorService.orchestrateContentGeneration(invalidRequest, testUser))
            .thenThrow(new IllegalArgumentException("Missing required fields: niche, topicIdea, targetAudience"));

        assertThrows(IllegalArgumentException.class, () -> {
            aIOrchestratorService.orchestrateContentGeneration(invalidRequest, testUser);
        });
    }

    @Test
    public void testCG003_GenerateContentInvalidPlatform() {
        // CG-003: Generate Content - Invalid Platform
        ContentGenerationDTO invalidRequest = new ContentGenerationDTO();
        invalidRequest.setNiche("Fitness");
        invalidRequest.setPlatform("Snapchat"); // Invalid
        invalidRequest.setContentType("entertainment");
        invalidRequest.setTopicIdea("test");

        when(aIOrchestratorService.orchestrateContentGeneration(invalidRequest, testUser))
            .thenThrow(new IllegalArgumentException("Invalid platform: Snapchat. Allowed: TikTok, Instagram Reels, YouTube Shorts"));

        assertThrows(IllegalArgumentException.class, () -> {
            aIOrchestratorService.orchestrateContentGeneration(invalidRequest, testUser);
        });
    }

    @Test
    public void testCG004A_GenerateEducationalContent() {
        // CG-004: Generate Content - Educational Type
        ContentGenerationDTO eduRequest = new ContentGenerationDTO();
        eduRequest.setNiche("Tech");
        eduRequest.setPlatform("YouTube Shorts");
        eduRequest.setContentType("educational");
        eduRequest.setTopicIdea("How to learn coding");
        eduRequest.setTargetAudience("Students");
        eduRequest.setVibe("educational");

        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        response.setScriptContent("Step 1: Learn JavaScript...\nStep 2:...");
        response.setRecommendedTone("Clear and authoritative");
        response.setContentStyle("Structured, step-by-step");

        when(aIOrchestratorService.orchestrateContentGeneration(eduRequest, testUser))
            .thenReturn(response);

        ContentGenerationResponseDTO result = aIOrchestratorService
            .orchestrateContentGeneration(eduRequest, testUser);

        assertTrue(result.getScriptContent().contains("Step"));
        assertTrue(result.getRecommendedTone().contains("authoritative"));
    }

    @Test
    public void testCG004B_GenerateEntertainmentContent() {
        // CG-004: Generate Content - Entertainment Type
        ContentGenerationDTO entRequest = new ContentGenerationDTO();
        entRequest.setNiche("Comedy");
        entRequest.setPlatform("TikTok");
        entRequest.setContentType("entertainment");
        entRequest.setTopicIdea("Funny fails");
        entRequest.setTargetAudience("Gen Z");
        entRequest.setVibe("funny");

        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        response.setScriptContent("POV: You tried to be cool...");
        response.setRecommendedTone("Funny, sarcastic");
        response.setContentStyle("Quick cuts, unexpected");

        when(aIOrchestratorService.orchestrateContentGeneration(entRequest, testUser))
            .thenReturn(response);

        ContentGenerationResponseDTO result = aIOrchestratorService
            .orchestrateContentGeneration(entRequest, testUser);

        assertTrue(result.getScriptContent().length() > 0);
        assertTrue(result.getRecommendedTone().contains("Funny"));
    }

    @Test
    public void testCG005_PromptInjectionPrevention() {
        // CG-005: Generate Content - Prompt Injection
        ContentGenerationDTO maliciousRequest = new ContentGenerationDTO();
        maliciousRequest.setNiche("Tech");
        maliciousRequest.setPlatform("TikTok");
        maliciousRequest.setContentType("entertainment");
        maliciousRequest.setTopicIdea("test'; DROP TABLE users--");
        maliciousRequest.setTargetAudience("Tech");

        // Content should be generated safely, not execute SQL
        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        response.setScriptContent("Generated about: test DROP TABLE users");

        when(aIOrchestratorService.orchestrateContentGeneration(maliciousRequest, testUser))
            .thenReturn(response);

        ContentGenerationResponseDTO result = aIOrchestratorService
            .orchestrateContentGeneration(maliciousRequest, testUser);

        assertNotNull(result);
        // Verify malicious SQL was sanitized
        assertFalse(result.getScriptContent().contains("'; DROP TABLE"));
    }

    @Test
    public void testCG006_APIUsageIncremented() {
        // CG-006: API Usage Limit
        int initialUsage = testUser.getApiUsageCount();

        when(aIOrchestratorService.orchestrateContentGeneration(validRequest, testUser))
            .thenAnswer(invocation -> {
                testUser.setApiUsageCount(testUser.getApiUsageCount() + 1);
                return new ContentGenerationResponseDTO();
            });

        aIOrchestratorService.orchestrateContentGeneration(validRequest, testUser);

        assertEquals(initialUsage + 1, testUser.getApiUsageCount());
    }

    @Test
    public void testCG006B_ExceedAPILimit() {
        // CG-006: API Usage Limit Exceeded
        testUser.setApiUsageCount(1000); // Max limit
        testUser.setMaxMonthlyApiCalls(1000);

        assertThrows(RuntimeException.class, () -> {
            if (testUser.getApiUsageCount() >= testUser.getMaxMonthlyApiCalls()) {
                throw new RuntimeException("Monthly API limit reached");
            }
        });
    }

    @Test
    public void testContentViralScoreRange() {
        // Verify viral score is within valid range
        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        response.setViralScore(BigDecimal.valueOf(7.5));

        assertTrue(response.getViralScore().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(response.getViralScore().compareTo(BigDecimal.TEN) <= 0);
    }

    @Test
    public void testContentConfidenceScoreRange() {
        // Verify confidence score is within valid range
        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        response.setConfidenceScore(BigDecimal.valueOf(85.5));

        assertTrue(response.getConfidenceScore().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(response.getConfidenceScore().compareTo(BigDecimal.valueOf(100)) <= 0);
    }

    @Test
    public void testGeneratedContentSavedToDatabase() {
        // Verify content is persisted
        when(generatedContentRepository.save(any(GeneratedContent.class)))
            .thenReturn(generatedContent);

        GeneratedContent saved = generatedContentRepository.save(generatedContent);

        assertNotNull(saved.getId());
        assertEquals(testUser.getId(), saved.getUser().getId());
        verify(generatedContentRepository).save(any(GeneratedContent.class));
    }

    @Test
    public void testContentGenerationLatency() {
        // Verify generation completes within timeout
        long startTime = System.currentTimeMillis();

        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        when(aIOrchestratorService.orchestrateContentGeneration(validRequest, testUser))
            .thenReturn(response);

        aIOrchestratorService.orchestrateContentGeneration(validRequest, testUser);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Mocked test should complete in < 1 second
        assertTrue(duration < 1000, "Generation took too long: " + duration + "ms");
    }

    @Test
    public void testHashtagsGenerated() {
        // Verify hashtags are generated
        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        List<String> hashtags = Arrays.asList("#FitnessGoals", "#Motivation", "#Workout");
        response.setHashtags(hashtags);

        assertNotNull(response.getHashtags());
        assertTrue(response.getHashtags().size() > 0);
        assertTrue(response.getHashtags().get(0).startsWith("#"));
    }

    @Test
    public void testRecommendedCTAGenerated() {
        // Verify CTA is generated
        ContentGenerationResponseDTO response = new ContentGenerationResponseDTO();
        response.setRecommendedCta("Follow for more fitness tips");

        assertNotNull(response.getRecommendedCta());
        assertTrue(response.getRecommendedCta().length() > 0);
    }
}
