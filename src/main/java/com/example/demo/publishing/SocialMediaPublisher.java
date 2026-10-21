package com.example.demo.publishing;

/**
 * Adapter boundary: return the platform's remote id or throw on a retryable API
 * failure.
 */
public interface SocialMediaPublisher {
    Platform platform();

    PublishResult publish(String content);
}
