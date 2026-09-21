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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        // Determine role
        Role role = Role.VOTER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = Role.ADMIN;
        }

        // Create user with hashed password
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role
        );

        User savedUser = userRepository.save(user);

        // Generate JWT
        String token = jwtUtil.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole().name(), savedUser.getId());
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password with BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if user is enabled
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(token, user.getUsername(), user.getRole().name(), user.getId());
    }

    public java.util.Map<String, Object> validateToken(String token) {
        if (!jwtUtil.isTokenValid(token) || jwtUtil.isTokenExpired(token)) {
            throw new InvalidCredentialsException("Token is invalid or expired");
        }
        return java.util.Map.of(
                "valid", true,
                "userId", jwtUtil.extractUserId(token),
                "username", jwtUtil.extractUsername(token),
                "role", jwtUtil.extractRole(token),
                "email", jwtUtil.extractEmail(token)
        );
    }
}
