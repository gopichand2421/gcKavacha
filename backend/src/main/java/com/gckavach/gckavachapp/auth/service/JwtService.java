package com.gckavach.gckavachapp.auth.service;

import com.gckavach.gckavachapp.config.JwtProperties;
import com.gckavach.gckavachapp.user.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = createSigningKey(jwtProperties.getSecret());
    }

    /**
     * Generate a JWT access token.
     */
    public String generateToken(
            String userId,
            List<Role> roles
    ) {
        Instant issuedAt = Instant.now();

        Instant expiresAt = issuedAt.plusSeconds(
                jwtProperties.getExpiration()
        );

        List<String> roleNames = roles.stream()
                .map(Role::name)
                .toList();

        return Jwts.builder()
                .subject(userId)
                .claim("roles", roleNames)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parse and validate a JWT.
     */
    public Jws<Claims> parseAndValidate(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Extract user ID from JWT.
     */
    public String extractUserId(String token) {

        return parseAndValidate(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extract roles from JWT.
     */
    public List<String> extractRoles(String token) {

        Object roles = parseAndValidate(token)
                .getPayload()
                .get("roles");

        if (roles instanceof List<?> roleList) {
            return roleList.stream()
                    .map(Object::toString)
                    .toList();
        }

        return List.of();
    }

    /**
     * Get configured expiration in seconds.
     */
    public long getExpiration() {
        return jwtProperties.getExpiration();
    }

    /**
     * Create a secure HMAC signing key.
     *
     * JJWT requires a minimum of 256 bits for HMAC-SHA algorithms.
     */
    private SecretKey createSigningKey(String secret) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET must be configured"
            );
        }

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes long"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}

