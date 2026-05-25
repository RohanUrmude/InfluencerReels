package com.viralforge.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    private int statusCode;

    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .statusCode(200)
            .build();
    }

    public static <T> ApiResponseDTO<T> error(String error, String message, int statusCode) {
        return ApiResponseDTO.<T>builder()
            .success(false)
            .message(message)
            .error(error)
            .timestamp(LocalDateTime.now())
            .statusCode(statusCode)
            .build();
    }
}
