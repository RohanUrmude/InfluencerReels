package com.viralforge.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private Integer apiUsageCount;
    private Integer maxMonthlyApiCalls;
}
