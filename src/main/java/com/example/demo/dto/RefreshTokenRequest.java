package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/** Carries a refresh token so the client can renew credentials without resending its password. */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}