package com.viralforge.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentGenerationResponseDTO {
    private Long contentId;
    private Long contentRequestId;

    @JsonProperty("scriptContent")
    private String scriptContent;

    @JsonProperty("hashtags")
    private List<String> hashtags;

    @JsonProperty("thumbnailText")
    private String thumbnailText;

    @JsonProperty("captions")
    private String captions;

    @JsonProperty("seoHashtags")
    private List<String> seoHashtags;

    @JsonProperty("postingSchedule")
    private String postingSchedule;

    @JsonProperty("engagementStrategy")
    private String engagementStrategy;

    @JsonProperty("growthTips")
    private String growthTips;

    @JsonProperty("bestPostingTime")
    private String bestPostingTime;

    @JsonProperty("platformOptimization")
    private String platformOptimization;

    @JsonProperty("viralScore")
    private BigDecimal viralScore;

    @JsonProperty("confidenceScore")
    private BigDecimal confidenceScore;

    @JsonProperty("primaryModelUsed")
    private String primaryModelUsed;

    @JsonProperty("fallbackModelUsed")
    private String fallbackModelUsed;

    @JsonProperty("generationLatencyMs")
    private Integer generationLatencyMs;

    @JsonProperty("audienceAnalysis")
    private AudienceAnalysisDTO audienceAnalysis;

    @JsonProperty("recommendedTone")
    private String recommendedTone;

    @JsonProperty("contentStyle")
    private String contentStyle;

    @JsonProperty("trendAlignment")
    private String trendAlignment;

    @JsonProperty("recommendedCTA")
    private String recommendedCta;

    @JsonProperty("generatedAt")
    private String generatedAt;
}
