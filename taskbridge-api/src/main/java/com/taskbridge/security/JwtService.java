package com.taskbridge.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Stateless JWT utility — validates and extracts claims.
 * Token generation is kept here for completeness but is typically
 * delegated to an auth service.
 */
@Component
public class JwtService {

    private final SecretKey secretKey;
    private final String    issuer;

    public JwtService(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.issuer}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer    = issuer;
    }

    /**
     * Validates signature, expiry, and issuer; returns all claims on success.
     *
     * @throws io.jsonwebtoken.JwtException on any validation failure
     */
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractSubject(Claims claims) {
        return claims.getSubject();
    }

    public UUID extractTenantId(Claims claims) {
        String raw = claims.get("tenant_id", String.class);
        if (raw == null) {
            throw new IllegalArgumentException("JWT missing required claim: tenant_id");
        }
        return UUID.fromString(raw);
    }
}

