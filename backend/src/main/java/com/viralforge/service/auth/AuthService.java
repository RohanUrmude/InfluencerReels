package com.viralforge.service.auth;

import com.viralforge.dto.request.LoginRequestDTO;
import com.viralforge.dto.request.RegisterRequestDTO;
import com.viralforge.dto.response.AuthResponseDTO;
import com.viralforge.entity.User;
import com.viralforge.exception.ValidationException;
import com.viralforge.repository.UserRepository;
import com.viralforge.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already taken");
        }

        User user = User.builder()
            .email(request.getEmail())
            .username(request.getUsername())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .niche(request.getNiche())
            .targetAudience(request.getTargetAudience())
            .preferredPlatform(request.getPreferredPlatform())
            .isActive(true)
            .apiUsageCount(0)
            .maxMonthlyApiCalls(1000)
            .build();

        user = userRepository.save(user);
        log.info("New user registered: {} ({})", user.getUsername(), user.getEmail());

        String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail());

        return AuthResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .apiUsageCount(user.getApiUsageCount())
            .maxMonthlyApiCalls(user.getMaxMonthlyApiCalls())
            .build();
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ValidationException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(authentication);
        log.info("User logged in: {}", user.getEmail());

        return AuthResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .apiUsageCount(user.getApiUsageCount())
            .maxMonthlyApiCalls(user.getMaxMonthlyApiCalls())
            .build();
    }
}
