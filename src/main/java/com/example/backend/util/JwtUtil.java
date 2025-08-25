package com.example.backend.util;

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

    @Value("${jwt.secret.key:084067a3d5f7cd51fd403224e262dfd83d52693505bb3f5aa578df2a90653693ae784a6b4ed7c1f74d2a7db9907a17792c1903acc4c334f4399e6b1a34f4f7043f13981dd5fbfd95e5e4946ae50b5639542c236dfc7f535047831f645515c22b52d8c3496ddd2b323e1110113dc42dbb5adb11e8f0f397127e736d3cf4b666c7415feef152338973ed7442282fa0a6d0380f233db271a7c523adf8ba94b66ff97567f26b9907c317db32f2b290b40b704fd7fb89ea5d9d992dbcb40bdfd3e352c1db716d655b5b5ce36c7bdefa29dfdff00c831ce83359dab68ad180b49ed6d1711bfa2102bed4b7f51740a8bf0fff51ca6aa4267011b4c65bdc3027034d30f0}")
    private String secretKey;

    private final long EXPIRATION_TIME = 86400000;

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
                .claim("roles", roles) // Roles already include ROLE_ prefix from AuthController
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
            throw e;
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
            throw e;
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
            return true;
        }
    }
}
