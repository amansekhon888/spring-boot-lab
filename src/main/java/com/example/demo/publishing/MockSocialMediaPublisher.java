package com.example.demo.publishing;

import java.util.UUID;

/**
 * Local demo adapter makes no network calls and needs no developer account or
 * paid service.
 */
public class MockSocialMediaPublisher implements SocialMediaPublisher {
    private final Platform platform;

    public MockSocialMediaPublisher(Platform platform) {
        this.platform = platform;
    }

    public Platform platform() {
        return platform;
    }

    public PublishResult publish(String content) {
        return new PublishResult("mock:" + platform.name().toLowerCase() + ":" + UUID.randomUUID(), "PUBLISHED");
    }
}
