package com.viralforge.controller;

import com.viralforge.dto.response.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> health() {
        Map<String, String> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "ViralForge AI");
        healthStatus.put("version", "1.0.0");

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponseDTO.success(healthStatus, "Service is healthy"));
    }
}
