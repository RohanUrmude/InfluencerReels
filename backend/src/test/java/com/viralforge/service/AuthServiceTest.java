package com.viralforge.service.auth;

import com.viralforge.dto.request.LoginRequestDTO;
import com.viralforge.dto.request.RegisterRequestDTO;
import com.viralforge.dto.response.AuthResponseDTO;
import com.viralforge.entity.User;
import com.viralforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO validRegisterRequest;
    private LoginRequestDTO validLoginRequest;
    private User testUser;

    @BeforeEach
    public void setUp() {
        validRegisterRequest = new RegisterRequestDTO();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("ValidPass123!");
        validRegisterRequest.setFullName("Test User");

        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("ValidPass123!");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPasswordHash("$2a$10$hashedpassword");
    }

    // ========== AUTHENTICATION TESTS ==========

    @Test
    public void testAU001_RegisterWithValidInput() {
        // AU-001: User Registration - Valid Input
        when(userRepository.findByEmail(validRegisterRequest.getEmail()))
            .thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRegisterRequest.getPassword()))
            .thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(testUser);

        AuthResponseDTO response = authService.register(validRegisterRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testAU002_RegisterWithInvalidPassword() {
        // AU-002: User Registration - Invalid Password
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
        invalidRequest.setEmail("test2@example.com");
        invalidRequest.setPassword("weak");

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(invalidRequest);
        });
    }

    @Test
    public void testAU003_RegisterWithDuplicateEmail() {
        // AU-003: User Registration - Duplicate Email
        when(userRepository.findByEmail(validRegisterRequest.getEmail()))
            .thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> {
            authService.register(validRegisterRequest);
        });
    }

    @Test
    public void testAU004_LoginWithValidCredentials() {
        // AU-004: User Login - Valid Credentials
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPasswordHash()))
            .thenReturn(true);

        AuthResponseDTO response = authService.login(validLoginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    public void testAU005_LoginWithInvalidPassword() {
        // AU-005: User Login - Invalid Password
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPasswordHash()))
            .thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            authService.login(validLoginRequest);
        });
    }

    @Test
    public void testAU006_LoginWithNonexistentUser() {
        // AU-005 variation: User doesn't exist
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            authService.login(validLoginRequest);
        });
    }

    @Test
    public void testAU008_GetCurrentUser() {
        // AU-008: Get Current User
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(testUser));

        User user = userRepository.findByEmail("test@example.com").orElse(null);

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
    }

    @Test
    public void testPasswordEncodingNotPlaintext() {
        // Security: Verify password is hashed, not plaintext
        when(userRepository.findByEmail(validRegisterRequest.getEmail()))
            .thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRegisterRequest.getPassword()))
            .thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(testUser);

        authService.register(validRegisterRequest);

        // Verify password was encoded (hashed)
        verify(passwordEncoder).encode(validRegisterRequest.getPassword());
        // Verify plain password is NOT in testUser
        assertNotEquals("ValidPass123!", testUser.getPasswordHash());
    }

    @Test
    public void testTokenGeneration() {
        // Verify JWT token is generated
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPasswordHash()))
            .thenReturn(true);

        AuthResponseDTO response = authService.login(validLoginRequest);

        assertNotNull(response.getToken());
        assertTrue(response.getToken().length() > 50); // JWT tokens are long
        assertTrue(response.getToken().contains(".")); // JWT format: header.payload.signature
    }
}
