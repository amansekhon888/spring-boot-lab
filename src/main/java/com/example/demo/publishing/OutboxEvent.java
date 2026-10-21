package com.example.demo.publishing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Written in the same transaction as QUEUED, then asynchronously relayed to
 * Kafka.
 */
@Entity
@Table(name = "publishing_outbox", indexes = @Index(name = "ix_outbox_pending", columnList = "sentAt,createdAt"))
public class OutboxEvent {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private Long publicationId;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    private Instant sentAt;

    protected OutboxEvent() {
    }

    public OutboxEvent(Long publicationId) {
        this.publicationId = publicationId;
    }

    public UUID getId() {
        return id;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void markSent() {
        sentAt = Instant.now();
    }
}
