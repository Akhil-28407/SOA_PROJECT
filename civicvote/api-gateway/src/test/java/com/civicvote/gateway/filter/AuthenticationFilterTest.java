package com.civicvote.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("API Gateway — Authentication & Security Filter Tests")
class AuthenticationFilterTest {

    private static final String SECRET = "civicvote-super-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private SecretKey secretKey;
    private AuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        filter = new AuthenticationFilter(SECRET);
    }

    private String generateToken(Long userId, String email, String username, String role, long validityMs) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "username", username, "role", role))
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(secretKey)
                .compact();
    }

    @Test
    @DisplayName("Public path (/auth/login) should pass through without token")
    void publicPath_ShouldPassWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(chainCalled.get(), "Gateway chain should be invoked for public paths");
        assertNotEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("CORS Preflight (OPTIONS) should pass through without token")
    void corsPreflight_ShouldPassWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.OPTIONS, "/votes")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(chainCalled.get(), "Gateway chain should be invoked for CORS preflight OPTIONS");
    }

    @Test
    @DisplayName("Direct external call to internal endpoint (POST /results/vote) should be blocked with 403")
    void internalEndpoint_ShouldBeBlocked() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/results/vote").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected path with missing Authorization header should return 401")
    void protectedPath_MissingAuthHeader_ShouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/elections").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected path with invalid JWT signature should return 401")
    void protectedPath_InvalidJwt_ShouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/elections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Admin path accessed by VOTER should return 403 Forbidden")
    void adminPath_AccessedByVoter_ShouldReturn403() {
        String voterToken = generateToken(10L, "voter@civicvote.com", "voter1", "VOTER", 3600000);

        MockServerHttpRequest request = MockServerHttpRequest.post("/elections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + voterToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Admin path accessed by ADMIN should pass and inject sanitized X-User-* headers")
    void adminPath_AccessedByAdmin_ShouldInjectHeadersAndPass() {
        String adminToken = generateToken(1L, "admin@civicvote.com", "adminUser", "ADMIN", 3600000);

        // Attempt spoofing by sending a fake user ID from client
        MockServerHttpRequest request = MockServerHttpRequest.post("/elections")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .header("X-User-Id", "9999-fake")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            ServerWebExchange mutatedExchange = ex;
            HttpHeaders downstreamHeaders = mutatedExchange.getRequest().getHeaders();

            // Verify header spoofing was prevented and verified JWT claims were injected
            assertEquals("1", downstreamHeaders.getFirst("X-User-Id"));
            assertEquals("ADMIN", downstreamHeaders.getFirst("X-User-Role"));
            assertEquals("admin@civicvote.com", downstreamHeaders.getFirst("X-User-Email"));
            assertEquals("adminUser", downstreamHeaders.getFirst("X-User-Name"));

            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(chainCalled.get(), "Chain should be invoked for authorized admin request");
    }
}
