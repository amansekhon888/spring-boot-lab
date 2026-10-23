package com.example.demo.publishing;

import jakarta.persistence.*;
import java.time.Instant;

/** One scheduled message and its simple status, stored in the database. */
@Entity
@Table(name = "scheduled_publications")
public class ScheduledPublication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;
    @Column(nullable = false, length = 3000)
    private String content;
    @Column(nullable = false)
    private Instant scheduledAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublishStatus status = PublishStatus.SCHEDULED;
    private String remoteId;

    protected ScheduledPublication() {
    }

    public ScheduledPublication(Platform platform, String content, Instant scheduledAt) {
        this.platform = platform;
        this.content = content;
        this.scheduledAt = scheduledAt;
    }

    public Long getId() {
        return id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getContent() {
        return content;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public PublishStatus getStatus() {
        return status;
    }

    public String getRemoteId() {
        return remoteId;
    }

    /** Called when the scheduler has handed this job to Kafka. */
    public void queued() {
        status = PublishStatus.QUEUED;
    }

    public void publishing() {
        status = PublishStatus.PUBLISHING;
    }

    public void published(String id) {
        status = PublishStatus.PUBLISHED;
        remoteId = id;
    }

    public void failed() {
        status = PublishStatus.FAILED;
    }
}
