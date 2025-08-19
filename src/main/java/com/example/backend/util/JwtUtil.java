package com.example.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key:XFC9UE8qxlJGqrQejS0UfoGdA8gB0Xua}") // Fallback to a secure default if not set
    private String secretKey;

    private final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured or is empty");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            System.out.println("Error extracting username from token: " + e.getMessage());
            throw e; // Re-throw to be handled by the caller
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            return (List<String>) Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("roles");
        } catch (Exception e) {
            System.out.println("Error extracting roles from token: " + e.getMessage());
            throw e; // Re-throw to be handled by the caller
        }
    }

    public Date extractIssuedAt(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getIssuedAt();
        } catch (Exception e) {
            System.out.println("Error extracting issuedAt from token: " + e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            boolean isExpired = isTokenExpired(token);
            System.out.println("Validating token - Username match: " + tokenUsername.equals(username) + ", Expired: " + isExpired);
            return tokenUsername.equals(username) && !isExpired;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            System.out.println("Error checking token expiration: " + e.getMessage());
            return true; // Treat as expired on error for security
        }
    }
}
