package com.example.demo.dto;

/** Returns the access credential and its longer-lived, refresh-only partner. */
public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}