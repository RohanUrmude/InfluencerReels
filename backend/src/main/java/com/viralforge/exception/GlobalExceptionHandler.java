package com.viralforge.exception;

import com.viralforge.dto.response.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleAIServiceException(
        AIServiceException ex,
        WebRequest request
    ) {
        log.error("AI Service error: ", ex);
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponseDTO.error(
                "AI_SERVICE_ERROR",
                ex.getMessage(),
                503
            ));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleUnauthorizedException(
        UnauthorizedException ex,
        WebRequest request
    ) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseDTO.error(
                "UNAUTHORIZED",
                ex.getMessage(),
                401
            ));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleValidationException(
        ValidationException ex,
        WebRequest request
    ) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseDTO.error(
                "VALIDATION_ERROR",
                ex.getMessage(),
                400
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseDTO.error(
                "VALIDATION_ERROR",
                "Invalid input parameters",
                400
            ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<String>> handleBadCredentialsException(
        BadCredentialsException ex,
        WebRequest request
    ) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseDTO.error(
                "INVALID_CREDENTIALS",
                "Email or password is incorrect",
                401
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<String>> handleGlobalException(
        Exception ex,
        WebRequest request
    ) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseDTO.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                500
            ));
    }
}
