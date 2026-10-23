package com.example.demo.publishing;

import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Local demo adapter makes no network calls and needs no developer account or
 * paid service.
 */
@Component
public class MockSocialMediaPublisher implements SocialMediaPublisher {
    public String publish(Platform platform, String content) {
        System.out.println("Mock publishing to " + platform + ": " + content);
        return "mock:" + platform.name().toLowerCase() + ":" + UUID.randomUUID();
    }
}
