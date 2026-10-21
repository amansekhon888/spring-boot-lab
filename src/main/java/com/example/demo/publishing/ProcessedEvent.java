package com.example.demo.publishing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_publish_events")
public class ProcessedEvent {
    @Id
    private UUID eventId;
    @Column(nullable = false)
    private Instant processedAt = Instant.now();

    protected ProcessedEvent() {
    }

    public ProcessedEvent(UUID id) {
        eventId = id;
    }
}
