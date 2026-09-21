package com.civicvote.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global JWT Authentication & Security Filter for API Gateway.
 *
 * Security Enhancements:
 * 1. CORS Preflight Bypass: Handles OPTIONS requests cleanly.
 * 2. Header Sanitization: Strips client-injected X-User-* headers before setting verified JWT claims.
 * 3. Internal Endpoint Shielding: Blocks direct external access to internal APIs (e.g., POST /results/vote).
 * 4. Role-Based Access Control: Enforces ADMIN privileges on election mutation routes.
 * 5. JWT Validation: Validates signature, expiry, and payload integrity.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    // Public paths that do not require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/actuator/health"
    );

    // Internal paths forbidden from external gateway invocation
    private static final List<PathRoleRule> INTERNAL_PATHS = List.of(
            new PathRoleRule("POST", "/results/vote")
    );

    // Paths restricted to ADMIN role
    private static final List<PathRoleRule> ADMIN_PATHS = List.of(
            new PathRoleRule("POST", "/elections"),
            new PathRoleRule("PUT", "/elections"),
            new PathRoleRule("DELETE", "/elections")
    );

    private final SecretKey secretKey;

    public AuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // 1. Allow CORS pre-flight requests to pass through
        if (CorsUtils.isPreFlightRequest(request)) {
            return chain.filter(exchange);
        }

        // 2. Block direct external access to internal endpoints
        if (isInternalPath(method, path)) {
            return onError(exchange, HttpStatus.FORBIDDEN, "Access to internal inter-service endpoints is prohibited");
        }

        // 3. Allow public paths without JWT
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 4. Extract and validate Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            // Validate JWT and extract claims
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = String.valueOf(claims.get("userId"));
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            String email = claims.getSubject();

            // 5. Role-based authorization for admin endpoints
            if (isAdminPath(method, path) && !"ADMIN".equals(role)) {
                return onError(exchange, HttpStatus.FORBIDDEN, "Admin access required");
            }

            // 6. Sanitize headers to prevent spoofing and propagate verified user identity downstream
            ServerHttpRequest modifiedRequest = request.mutate()
                    .headers(httpHeaders -> {
                        httpHeaders.remove("X-User-Id");
                        httpHeaders.remove("X-User-Role");
                        httpHeaders.remove("X-User-Email");
                        httpHeaders.remove("X-User-Name");
                        httpHeaders.set("X-User-Id", userId);
                        httpHeaders.set("X-User-Role", role);
                        httpHeaders.set("X-User-Email", email != null ? email : "");
                        httpHeaders.set("X-User-Name", username != null ? username : "");
                    })
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isInternalPath(String method, String path) {
        return INTERNAL_PATHS.stream().anyMatch(rule ->
                rule.method.equalsIgnoreCase(method) && path.startsWith(rule.pathPrefix));
    }

    private boolean isAdminPath(String method, String path) {
        return ADMIN_PATHS.stream().anyMatch(rule ->
                rule.method.equalsIgnoreCase(method) && path.startsWith(rule.pathPrefix));
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                java.time.LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }

    private record PathRoleRule(String method, String pathPrefix) {}
}
