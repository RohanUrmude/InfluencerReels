package com.viralforge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthStrategyDTO {
    private String bestPostingTime;
    private String postingSchedule;
    private List<String> seoHashtags;
    private String thumbnailText;
    private String captionHook;
    private List<String> engagementTriggers;
    private String engagementStrategy;
    private String platformOptimization;
    private String crossPlatformStrategy;
    private String collaborationOpportunities;
    private String seriesIdea;
    private String trendingAudio;
    private List<String> analyticsToTrack;
    private List<String> growthHacks;
}
