package com.example.demo.publishing;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;

/**
 * Real mode intentionally fails closed until account ids, OAuth tokens, and
 * approvals are configured.
 */
@Component
@Profile("real-publishers")
class LinkedInPublisher implements SocialMediaPublisher {
    public Platform platform() {
        return Platform.LINKEDIN;
    }

    public PublishResult publish(String content) {
        String token = required("LINKEDIN_ACCESS_TOKEN");
        String author = required("LINKEDIN_AUTHOR_URN");
        // The API requires a version header; require operators to choose one supported now.
        String version = required("LINKEDIN_API_VERSION");
        var response = RestClient.create("https://api.linkedin.com").post().uri("/rest/posts")
                .header("Authorization", "Bearer " + token)
                .header("Linkedin-Version", version)
                .header("X-Restli-Protocol-Version", "2.0.0")
                .body(java.util.Map.of("author", author, "commentary", content, "visibility", "PUBLIC",
                        "lifecycleState", "PUBLISHED", "distribution", java.util.Map.of("feedDistribution", "MAIN_FEED",
                                "targetEntities", List.of(), "thirdPartyDistributionChannels", List.of())))
                .retrieve().toBodilessEntity();
        return new PublishResult(response.getHeaders().getFirst("x-restli-id"), "PUBLISHED");
    }

    static String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank())
            throw new IllegalStateException("Missing environment credential: " + key);
        return value;
    }
}

@Component
@Profile("real-publishers")
class InstagramPublisher implements SocialMediaPublisher {
    public Platform platform() {
        return Platform.INSTAGRAM;
    }

    public PublishResult publish(String content) {
        String token = LinkedInPublisher.required("INSTAGRAM_ACCESS_TOKEN");
        String account = LinkedInPublisher.required("INSTAGRAM_BUSINESS_ACCOUNT_ID");
        throw new UnsupportedOperationException(
                "Instagram Graph publishing requires an image/video media URL; configure the media publishing workflow before enabling this adapter (account "
                        + account + ", token configured=" + !token.isBlank() + ")");
    }
}

@Component
@Profile("real-publishers")
class XPublisher implements SocialMediaPublisher {
    public Platform platform() {
        return Platform.X;
    }

    public PublishResult publish(String content) {
        String token = LinkedInPublisher.required("X_BEARER_TOKEN");
        var body = java.util.Map.of("text", content);
        var response = RestClient.create("https://api.x.com").post().uri("/2/tweets")
                .header("Authorization", "Bearer " + token).body(body).retrieve().body(java.util.Map.class);
        Object data = response == null ? null : response.get("data");
        Object id = data instanceof java.util.Map<?, ?> map ? map.get("id") : null;
        return new PublishResult(String.valueOf(id), "PUBLISHED");
    }
}
