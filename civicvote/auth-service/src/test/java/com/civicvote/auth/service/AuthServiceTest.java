package com.civicvote.auth.service;

import com.civicvote.auth.dto.AuthResponse;
import com.civicvote.auth.dto.LoginRequest;
import com.civicvote.auth.dto.RegisterRequest;
import com.civicvote.auth.entity.Role;
import com.civicvote.auth.entity.User;
import com.civicvote.auth.exception.DuplicateResourceException;
import com.civicvote.auth.exception.InvalidCredentialsException;
import com.civicvote.auth.repository.UserRepository;
import com.civicvote.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service — Core Business Logic Tests")
class AuthServiceTest {

    private static final String SECRET = "civicvote-super-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION_MS = 3600000;

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_MS);
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@civicvote.com");
        registerRequest.setPassword("securePassword123");
        registerRequest.setRole("VOTER");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@civicvote.com");
        loginRequest.setPassword("securePassword123");

        testUser = new User("testuser", "test@civicvote.com", passwordEncoder.encode("securePassword123"), Role.VOTER);
        testUser.setId(100L);
    }

    @Test
    @DisplayName("Successful registration saves user with hashed password and returns JWT")
    void register_Success() {
        when(userRepository.existsByEmail("test@civicvote.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("VOTER", response.getRole());
        assertEquals(100L, response.getUserId());

        // Verify token carries correct user details
        assertTrue(jwtUtil.isTokenValid(response.getToken()));
        assertEquals(100L, jwtUtil.extractUserId(response.getToken()));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Registration with existing email throws DuplicateResourceException")
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("test@civicvote.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration with existing username throws DuplicateResourceException")
    void register_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByEmail("test@civicvote.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Successful login verifies BCrypt password and returns JWT token and user details")
    void login_Success() {
        when(userRepository.findByEmail("test@civicvote.com")).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals(100L, response.getUserId());
        assertEquals("VOTER", response.getRole());
    }

    @Test
    @DisplayName("Login with wrong password throws InvalidCredentialsException")
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail("test@civicvote.com")).thenReturn(Optional.of(testUser));

        loginRequest.setPassword("wrongPassword");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Login with non-existent email throws InvalidCredentialsException")
    void login_NonExistentEmail_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Validate token returns user metadata when token is valid")
    void validateToken_Success() {
        String token = jwtUtil.generateToken(100L, "test@civicvote.com", "testuser", "VOTER");

        Map<String, Object> result = authService.validateToken(token);

        assertTrue((Boolean) result.get("valid"));
        assertEquals(100L, result.get("userId"));
        assertEquals("testuser", result.get("username"));
        assertEquals("VOTER", result.get("role"));
        assertEquals("test@civicvote.com", result.get("email"));
    }

    @Test
    @DisplayName("Validate token throws InvalidCredentialsException when token is expired or invalid")
    void validateToken_Invalid_ThrowsException() {
        String invalidToken = "invalid.token.here";

        assertThrows(InvalidCredentialsException.class, () -> authService.validateToken(invalidToken));
    }
}
