package com.example.demo.publishing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class PublishingService {
    private final PublicationRepository publications;
    private final OutboxRepository outbox;
    private final ProcessedEventRepository processed;
    private final List<SocialMediaPublisher> publishers;

    public PublishingService(PublicationRepository p, OutboxRepository o, ProcessedEventRepository e,
            List<SocialMediaPublisher> publishers) {
        this.publications = p;
        this.outbox = o;
        this.processed = e;
        this.publishers = publishers;
    }

    @Transactional
    public ScheduledPublication schedule(Platform platform, String content, Instant at) {
        return publications.save(new ScheduledPublication(platform, content, at));
    }

    /**
     * Lock and state transition plus outbox insert commit together, so two
     * scheduler instances cannot both enqueue.
     */
    @Transactional
    public int enqueueDue(int batchSize, Duration lease) {
        Instant now = Instant.now();
        recoverExpired(now);
        var due = publications.lockDue(List.of(PublishStatus.SCHEDULED, PublishStatus.RETRY), now,
                org.springframework.data.domain.PageRequest.of(0, batchSize));
        for (var job : due) {
            job.claim(now.plus(lease));
            outbox.save(new OutboxEvent(job.getId()));
        }
        return due.size();
    }

    private void recoverExpired(Instant now) {
        for (var job : publications.lockExpired(PublishStatus.QUEUED, now,
                org.springframework.data.domain.PageRequest.of(0, 25)))
            job.retry("Scheduler lease expired; safe to enqueue again");
    }

    @Transactional
    public void publish(PublishEvent event) {
        if (processed.existsById(event.eventId()))
            return;
        // Insert before the external side effect so a uniqueness constraint serializes concurrent copies.
        // If publishing fails, this transaction rolls the claim back and Kafka may retry the event.
        processed.saveAndFlush(new ProcessedEvent(event.eventId()));
        var job = publications.findById(event.publicationId()).orElseThrow();
        if (job.getStatus() == PublishStatus.PUBLISHED) {
            return;
        }
        job.publishing();
        try {
            SocialMediaPublisher publisher = publishers.stream().filter(p -> p.platform() == job.getPlatform())
                    .findFirst().orElseThrow();
            PublishResult result = publisher.publish(job.getContent());
            job.published(result.remoteId());
        } catch (RuntimeException ex) {
            job.retry(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            throw ex;
        }
    }
}
