package com.example.demo.publishing;

import java.util.UUID;

/**
 * Kafka contract carries a stable event id; consumers use it as an idempotency
 * key.
 */
public record PublishEvent(UUID eventId, Long publicationId) {
}
