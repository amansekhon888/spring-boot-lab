package com.example.demo.publishing;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Duration;

/**
 * Polling is intentionally short and bounded; Kafka and platform calls never
 * run inside the claim transaction.
 */
@Component
public class PublishingScheduler {
    private final PublishingService service;

    public PublishingScheduler(PublishingService s) {
        service = s;
    }

    @Scheduled(fixedDelayString = "${app.publishing.poll-ms:3000}")
    public void poll() {
        service.enqueueDue(25, Duration.ofMinutes(2));
    }
}
