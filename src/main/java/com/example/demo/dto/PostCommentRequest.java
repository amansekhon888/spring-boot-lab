package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Validated body for adding a comment to a post. */
public record PostCommentRequest(@NotBlank @Size(max = 1000) String body) {
}