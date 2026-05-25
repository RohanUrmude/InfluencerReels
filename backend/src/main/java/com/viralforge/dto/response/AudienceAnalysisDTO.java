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
public class AudienceAnalysisDTO {
    @JsonProperty("audienceType")
    private String audienceType;

    @JsonProperty("viralPotential")
    private BigDecimal viralPotential;

    @JsonProperty("confidenceScore")
    private BigDecimal confidenceScore;

    @JsonProperty("recommendedTone")
    private String recommendedTone;

    @JsonProperty("contentStyle")
    private String contentStyle;

    @JsonProperty("engagementTriggers")
    private List<String> engagementTriggers;

    @JsonProperty("hashtags")
    private List<String> hashtags;

    @JsonProperty("trendAlignment")
    private String trendAlignment;

    @JsonProperty("viralHooks")
    private List<String> viralHooks;

    @JsonProperty("recommendedCTA")
    private String recommendedCta;
}
