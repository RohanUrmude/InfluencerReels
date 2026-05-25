package com.viralforge.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viralforge.dto.request.ContentGenerationDTO;
import com.viralforge.dto.response.AudienceAnalysisDTO;
import com.viralforge.dto.response.ContentGenerationResponseDTO;
import com.viralforge.entity.*;
import com.viralforge.exception.AIServiceException;
import com.viralforge.repository.*;
import com.viralforge.util.RetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class AIOrchestratorService {
    @Autowired
    private PhiService phiService;

    @Autowired
    private LlamaService llamaService;

    @Autowired
    private MistralService mistralService;

    @Autowired
    private AIRouterService aiRouterService;

    @Autowired
    private RetryService retryService;

    @Autowired
    private ContentRequestRepository contentRequestRepository;

    @Autowired
    private GeneratedContentRepository generatedContentRepository;

    @Autowired
    private AIUsageLogRepository aiUsageLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContentGenerationResponseDTO orchestrateContentGeneration(
        ContentGenerationDTO request,
        User user
    ) throws AIServiceException {
        long orchestrationStart = System.currentTimeMillis();
        log.info("=== Starting AI content generation orchestration for user: {} ===", user.getId());

        ContentRequest contentRequest = createContentRequest(request, user);

        try {
            // STEP 1: AUDIENCE ANALYSIS (PHI Model)
            log.info("STEP 1: Running audience analysis with Phi model...");
            AudienceAnalysisDTO audienceAnalysis = executeWithRetry(
                () -> phiService.analyzeAudience(
                    request.getNiche(),
                    request.getVibe(),
                    request.getTopicIdea(),
                    request.getTargetAudience(),
                    request.getPlatform()
                ),
                "Phi-Audience-Analysis"
            );

            // STEP 2: CONDITIONAL SCRIPT GENERATION (Based on content type)
            log.info("STEP 2: Generating script based on content type: {}", request.getContentType());
            String scriptContent;
            String primaryModel;
            String fallbackModel = null;

            if ("educational".equalsIgnoreCase(request.getContentType())) {
                primaryModel = "meta-llama/Meta-Llama-3-8B-Instruct";
                try {
                    scriptContent = executeWithRetry(
                        () -> llamaService.generateEducationalScript(
                            request.getTopicIdea(),
                            request.getTargetAudience(),
                            request.getPlatform(),
                            request.getVibe(),
                            request.getCreatorGoal()
                        ),
                        "Llama-Educational-Script"
                    );
                    log.info("Educational script generated successfully with Llama");
                } catch (AIServiceException e) {
                    log.warn("Llama model failed, falling back to Mistral: {}", e.getMessage());
                    fallbackModel = "mistralai/Mistral-7B-Instruct-v0.2";
                    scriptContent = executeWithRetry(
                        () -> mistralService.generateViralEntertainmentScript(
                            request.getTopicIdea(),
                            request.getTargetAudience(),
                            request.getPlatform(),
                            request.getVibe(),
                            request.getCreatorGoal()
                        ),
                        "Mistral-Fallback-Script"
                    );
                    aiRouterService.reportModelFailure("meta-llama/Meta-Llama-3-8B-Instruct", e.getMessage());
                }
            } else {
                primaryModel = "mistralai/Mistral-7B-Instruct-v0.2";
                try {
                    scriptContent = executeWithRetry(
                        () -> mistralService.generateViralEntertainmentScript(
                            request.getTopicIdea(),
                            request.getTargetAudience(),
                            request.getPlatform(),
                            request.getVibe(),
                            request.getCreatorGoal()
                        ),
                        "Mistral-Entertainment-Script"
                    );
                    log.info("Entertainment script generated successfully with Mistral");
                } catch (AIServiceException e) {
                    log.warn("Mistral model failed, falling back to Llama: {}", e.getMessage());
                    fallbackModel = "meta-llama/Meta-Llama-3-8B-Instruct";
                    scriptContent = executeWithRetry(
                        () -> llamaService.generateEducationalScript(
                            request.getTopicIdea(),
                            request.getTargetAudience(),
                            request.getPlatform(),
                            request.getVibe(),
                            request.getCreatorGoal()
                        ),
                        "Llama-Fallback-Script"
                    );
                    aiRouterService.reportModelFailure("mistralai/Mistral-7B-Instruct-v0.2", e.getMessage());
                }
            }

            // STEP 3: GROWTH STRATEGY GENERATION (Mistral)
            log.info("STEP 3: Generating growth strategy with Mistral model...");
            String growthStrategy = executeWithRetry(
                () -> mistralService.generateGrowthStrategy(
                    request.getNiche(),
                    request.getPlatform(),
                    request.getTopicIdea(),
                    request.getContentType()
                ),
                "Mistral-Growth-Strategy"
            );

            long orchestrationLatency = System.currentTimeMillis() - orchestrationStart;

            // Parse and build response
            ContentGenerationResponseDTO response = buildContentResponse(
                contentRequest,
                audienceAnalysis,
                scriptContent,
                growthStrategy,
                primaryModel,
                fallbackModel,
                orchestrationLatency
            );

            // Save generated content
            GeneratedContent generatedContent = saveGeneratedContent(
                contentRequest,
                user,
                response,
                primaryModel,
                fallbackModel
            );

            response.setContentId(generatedContent.getId());
            response.setContentRequestId(contentRequest.getId());

            // Update API usage count
            user.setApiUsageCount((user.getApiUsageCount() != null ? user.getApiUsageCount() : 0) + 1);
            user.setUpdatedAt(LocalDateTime.now());
            // Note: User will be saved when returned in the response

            log.info("=== AI content generation completed successfully in {}ms ===", orchestrationLatency);
            log.info("API Usage updated: {}/{}", user.getApiUsageCount(), user.getMaxMonthlyApiCalls());
            return response;

        } catch (Exception e) {
            contentRequest.setStatus("FAILED");
            contentRequestRepository.save(contentRequest);
            log.error("Content generation orchestration failed", e);
            throw new AIServiceException("Content generation failed: " + e.getMessage(), e);
        }
    }

    private <T> T executeWithRetry(RetryService.Callable<T> callable, String operationName) throws AIServiceException {
        return retryService.executeWithRetry(callable, 3, 1000, operationName);
    }

    private ContentRequest createContentRequest(ContentGenerationDTO request, User user) {
        ContentRequest contentRequest = ContentRequest.builder()
            .user(user)
            .niche(request.getNiche())
            .targetAudience(request.getTargetAudience())
            .vibe(request.getVibe())
            .platform(request.getPlatform())
            .topicIdea(request.getTopicIdea())
            .contentType(request.getContentType())
            .creatorGoal(request.getCreatorGoal())
            .status("PROCESSING")
            .build();

        return contentRequestRepository.save(contentRequest);
    }

    private ContentGenerationResponseDTO buildContentResponse(
        ContentRequest contentRequest,
        AudienceAnalysisDTO audienceAnalysis,
        String scriptContent,
        String growthStrategy,
        String primaryModel,
        String fallbackModel,
        long latency
    ) {
        return ContentGenerationResponseDTO.builder()
            .contentRequestId(contentRequest.getId())
            .scriptContent(scriptContent)
            .audienceAnalysis(audienceAnalysis)
            .viralScore(audienceAnalysis.getViralPotential())
            .confidenceScore(audienceAnalysis.getConfidenceScore())
            .primaryModelUsed(primaryModel)
            .fallbackModelUsed(fallbackModel)
            .generationLatencyMs((int) latency)
            .recommendedTone(audienceAnalysis.getRecommendedTone())
            .contentStyle(audienceAnalysis.getContentStyle())
            .trendAlignment(audienceAnalysis.getTrendAlignment())
            .recommendedCta(audienceAnalysis.getRecommendedCta())
            .hashtags(audienceAnalysis.getHashtags())
            .seoHashtags(getHashtagsFromStrategy(growthStrategy))
            .generatedAt(LocalDateTime.now().toString())
            .build();
    }

    private GeneratedContent saveGeneratedContent(
        ContentRequest contentRequest,
        User user,
        ContentGenerationResponseDTO response,
        String primaryModel,
        String fallbackModel
    ) {
        GeneratedContent content = GeneratedContent.builder()
            .contentRequest(contentRequest)
            .user(user)
            .scriptContent(response.getScriptContent())
            .audienceAnalysis(objectMapper.valueToTree(response.getAudienceAnalysis()))
            .hashtags(response.getHashtags().toArray(new String[0]))
            .seoHashtags(response.getSeoHashtags().toArray(new String[0]))
            .viralScore(response.getViralScore())
            .confidenceScore(response.getConfidenceScore())
            .primaryModelUsed(primaryModel)
            .fallbackModelUsed(fallbackModel)
            .generationLatencyMs(response.getGenerationLatencyMs())
            .audienceType(response.getAudienceAnalysis().getAudienceType())
            .recommendedTone(response.getRecommendedTone())
            .contentStyle(response.getContentStyle())
            .trendAlignment(response.getTrendAlignment())
            .recommendedCta(response.getRecommendedCta())
            .engagementTriggers(response.getAudienceAnalysis().getEngagementTriggers().toArray(new String[0]))
            .viralHooks(response.getAudienceAnalysis().getViralHooks().toArray(new String[0]))
            .build();

        return generatedContentRepository.save(content);
    }

    private List<String> getHashtagsFromStrategy(String strategy) {
        return Arrays.asList("#viral", "#trending", "#creator", "#content");
    }
}
