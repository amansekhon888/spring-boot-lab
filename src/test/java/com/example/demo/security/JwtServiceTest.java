package com.example.demo.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies signature validation and ensures refresh credentials cannot masquerade as access tokens. */
class JwtServiceTest {

    private static final String TEST_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void accessAndRefreshTokensAreDifferentCredentialTypes() {
        JwtService jwtService = new JwtService(TEST_KEY, 15, 7);
        UserDetails user = User.withUsername("student").password("{noop}password").roles("USER").build();
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        assertTrue(jwtService.isValid(accessToken, user, "access"));
        assertTrue(jwtService.isValid(refreshToken, user, "refresh"));
        assertFalse(jwtService.isValid(refreshToken, user, "access"));
        assertFalse(jwtService.isValid(accessToken, User.withUsername("other").password("{noop}x").build(), "access"));
    }
}