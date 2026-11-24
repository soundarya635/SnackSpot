package com.example.snackspotApi.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String VALID_TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", VALID_TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void testGenerateAndValidateToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails, "USER");

        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.structure"));
    }

    @Test
    void testTamperedToken() {
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails, "USER");
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(jwtService.validateToken(tamperedToken));
        assertFalse(jwtService.isTokenValid(tamperedToken, userDetails));
    }

    @Test
    void testExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L); // expired 1s ago
        UserDetails userDetails = new User("expireduser", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails, "USER");

        assertFalse(jwtService.validateToken(token));
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testFailsSafelyWhenSecretMissingOrTooShort() {
        JwtService missingSecretService = new JwtService();
        ReflectionTestUtils.setField(missingSecretService, "secretKey", "");
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> missingSecretService.generateToken(userDetails, "USER"));

        JwtService shortSecretService = new JwtService();
        ReflectionTestUtils.setField(shortSecretService, "secretKey", "shortSecret123");
        assertThrows(IllegalStateException.class, () -> shortSecretService.generateToken(userDetails, "USER"));
    }
}
