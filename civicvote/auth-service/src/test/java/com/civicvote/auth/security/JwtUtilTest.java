package com.civicvote.auth.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auth Service — JWT Utility Tests")
class JwtUtilTest {

    private static final String SECRET = "civicvote-super-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Generate token with valid claims and verify all claims extraction")
    void generateToken_ShouldContainValidClaims() {
        String token = jwtUtil.generateToken(42L, "alice@civicvote.com", "alice", "VOTER");

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(42L, jwtUtil.extractUserId(token));
        assertEquals("alice@civicvote.com", jwtUtil.extractEmail(token));
        assertEquals("alice", jwtUtil.extractUsername(token));
        assertEquals("VOTER", jwtUtil.extractRole(token));
        assertTrue(jwtUtil.isTokenValid(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("Generate admin token and verify role claim is ADMIN")
    void generateAdminToken_ShouldHaveAdminRole() {
        String token = jwtUtil.generateToken(1L, "admin@civicvote.com", "admin", "ADMIN");

        assertEquals("ADMIN", jwtUtil.extractRole(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
    }

    @Test
    @DisplayName("Expired token should be recognized as expired")
    void expiredToken_ShouldBeDetected() {
        // JwtUtil with negative expiration
        JwtUtil expiredUtil = new JwtUtil(SECRET, -1000);
        String expiredToken = expiredUtil.generateToken(99L, "expired@test.com", "expired", "VOTER");

        assertFalse(jwtUtil.isTokenValid(expiredToken));
    }

    @Test
    @DisplayName("Tampered token signature should fail validation")
    void tamperedToken_ShouldFailValidation() {
        String validToken = jwtUtil.generateToken(10L, "tamper@test.com", "tamper", "VOTER");
        String tamperedToken = validToken + "corrupted";

        assertFalse(jwtUtil.isTokenValid(tamperedToken));
    }

    @Test
    @DisplayName("Extract all claims parses correctly")
    void extractAllClaims_ParsesSubjectAndIssuedAt() {
        String token = jwtUtil.generateToken(7L, "user7@civicvote.com", "user7", "VOTER");
        Claims claims = jwtUtil.extractAllClaims(token);

        assertEquals("user7@civicvote.com", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}
