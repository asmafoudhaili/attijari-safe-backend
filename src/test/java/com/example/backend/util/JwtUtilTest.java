package com.example.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtUtil
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecretKey = "testsecretkey123456789012345678901234567890123456789012345678901234567890";
    private final long testExpirationTime = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", testExpirationTime);
        jwtUtil.init();
    }

    @Test
    @DisplayName("Should generate valid token with username and roles")
    void shouldGenerateValidTokenWithUsernameAndRoles() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_USER");

        // When
        String token = jwtUtil.generateToken(username, roles);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(username);
        assertThat(jwtUtil.extractRoles(token)).containsExactlyInAnyOrderElementsOf(roles);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when generating token with null username")
    void shouldThrowExceptionWhenGeneratingTokenWithNullUsername() {
        // Given
        List<String> roles = Arrays.asList("ROLE_ADMIN");

        // When & Then
        assertThatThrownBy(() -> jwtUtil.generateToken(null, roles))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when generating token with empty username")
    void shouldThrowExceptionWhenGeneratingTokenWithEmptyUsername() {
        // Given
        List<String> roles = Arrays.asList("ROLE_ADMIN");

        // When & Then
        assertThatThrownBy(() -> jwtUtil.generateToken("", roles))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when generating token with null roles")
    void shouldThrowExceptionWhenGeneratingTokenWithNullRoles() {
        // Given
        String username = "testuser";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.generateToken(username, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Roles cannot be null");
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void shouldExtractUsernameFromValidToken() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ROLE_ADMIN");
        String token = jwtUtil.generateToken(username, roles);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should throw exception when extracting username from invalid token")
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid JWT token");
    }

    @Test
    @DisplayName("Should extract roles from valid token")
    void shouldExtractRolesFromValidToken() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_USER");
        String token = jwtUtil.generateToken(username, roles);

        // When
        List<String> extractedRoles = jwtUtil.extractRoles(token);

        // Then
        assertThat(extractedRoles).containsExactlyInAnyOrderElementsOf(roles);
    }

    @Test
    @DisplayName("Should throw exception when extracting roles from invalid token")
    void shouldThrowExceptionWhenExtractingRolesFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractRoles(invalidToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid JWT token");
    }

    @Test
    @DisplayName("Should extract issued at date from valid token")
    void shouldExtractIssuedAtFromValidToken() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ROLE_ADMIN");
        String token = jwtUtil.generateToken(username, roles);

        // When
        Date issuedAt = jwtUtil.extractIssuedAt(token);

        // Then
        assertThat(issuedAt).isNotNull();
        assertThat(issuedAt).isBeforeOrEqualTo(new Date());
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ROLE_ADMIN");
        String token = jwtUtil.generateToken(username, roles);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should validate token with username successfully")
    void shouldValidateTokenWithUsernameSuccessfully() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ROLE_ADMIN");
        String token = jwtUtil.generateToken(username, roles);

        // When
        boolean isValid = jwtUtil.validateToken(token, username);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false when validating token with wrong username")
    void shouldReturnFalseWhenValidatingTokenWithWrongUsername() {
        // Given
        String username = "testuser";
        String wrongUsername = "wronguser";
        List<String> roles = Arrays.asList("ROLE_ADMIN");
        String token = jwtUtil.generateToken(username, roles);

        // When
        boolean isValid = jwtUtil.validateToken(token, wrongUsername);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when validating invalid token")
    void shouldReturnFalseWhenValidatingInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when validating invalid token with username")
    void shouldReturnFalseWhenValidatingInvalidTokenWithUsername() {
        // Given
        String invalidToken = "invalid.token.here";
        String username = "testuser";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken, username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid JWT token");
    }
}
