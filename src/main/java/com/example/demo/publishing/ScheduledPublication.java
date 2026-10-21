package com.example.demo.publishing;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A publishable unit. leaseUntil makes abandoned claims recoverable after a
 * process crash.
 */
@Entity
@Table(name = "scheduled_publications", indexes = @Index(name = "ix_pub_due", columnList = "status,scheduled_at"))
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
    private Instant leaseUntil;
    private int attempts;
    private String remoteId;
    @Column(length = 1000)
    private String lastError;

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

    public int getAttempts() {
        return attempts;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public String getLastError() {
        return lastError;
    }

    public void claim(Instant until) {
        status = PublishStatus.QUEUED;
        leaseUntil = until;
        attempts++;
    }

    public boolean leaseExpired(Instant now) {
        return status == PublishStatus.QUEUED && leaseUntil != null && leaseUntil.isBefore(now);
    }

    public void publishing() {
        status = PublishStatus.PUBLISHING;
    }

    public void published(String id) {
        status = PublishStatus.PUBLISHED;
        remoteId = id;
        leaseUntil = null;
        lastError = null;
    }

    public void retry(String error) {
        status = PublishStatus.RETRY;
        lastError = error;
        leaseUntil = null;
    }

    public void failed(String error) {
        status = PublishStatus.FAILED;
        lastError = error;
        leaseUntil = null;
    }
}
