package com.viralforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentGenerationDTO {
    @NotBlank(message = "Niche is required")
    @Size(min = 2, max = 100, message = "Niche must be between 2 and 100 characters")
    private String niche;

    @NotBlank(message = "Target audience is required")
    @Size(min = 2, max = 100, message = "Target audience must be between 2 and 100 characters")
    private String targetAudience;

    @Size(max = 100, message = "Vibe must be at most 100 characters")
    private String vibe;

    @NotNull(message = "Platform is required")
    private String platform; // Instagram Reels, TikTok, YouTube Shorts

    @NotBlank(message = "Topic idea is required")
    @Size(min = 5, max = 1000, message = "Topic idea must be between 5 and 1000 characters")
    private String topicIdea;

    @NotNull(message = "Content type is required")
    private String contentType; // Educational, Entertainment

    @Size(max = 500, message = "Creator goal must be at most 500 characters")
    private String creatorGoal;
}
