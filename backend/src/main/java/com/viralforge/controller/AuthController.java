package com.viralforge.controller;

import com.viralforge.dto.request.LoginRequestDTO;
import com.viralforge.dto.request.RegisterRequestDTO;
import com.viralforge.dto.response.ApiResponseDTO;
import com.viralforge.dto.response.AuthResponseDTO;
import com.viralforge.entity.User;
import com.viralforge.repository.UserRepository;
import com.viralforge.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> register(
        @Valid @RequestBody RegisterRequestDTO request
    ) {
        log.info("Registration request for user: {}", request.getEmail());
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponseDTO.success(response, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(
        @Valid @RequestBody LoginRequestDTO request
    ) {
        log.info("Login request for user: {}", request.getEmail());
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(response, "Login successful"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<User>> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(user, "User data retrieved successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponseDTO<String>> health() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success("OK", "Auth service is healthy"));
    }
}
