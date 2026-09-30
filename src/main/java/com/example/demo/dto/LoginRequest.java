package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/** Credentials sent once to exchange a username/password for short-lived bearer tokens. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}