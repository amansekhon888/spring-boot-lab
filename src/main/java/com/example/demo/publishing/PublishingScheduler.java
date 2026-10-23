package com.example.demo.publishing;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A timer asks the service to find a few due jobs and put their ids on Kafka.
 */
@Component
public class PublishingScheduler {
    private final PublicationRepository jobs;
    private final PublishingProducer producer;

    public PublishingScheduler(PublicationRepository jobs, PublishingProducer producer) {
        this.jobs = jobs;
        this.producer = producer;
    }

    @Scheduled(fixedDelayString = "${app.publishing.poll-ms:3000}")
    public void poll() {
        var dueJobs = jobs.findByStatusAndScheduledAtBeforeOrderByScheduledAt(
                PublishStatus.SCHEDULED, java.time.Instant.now(), org.springframework.data.domain.PageRequest.of(0, 10));
        for (var job : dueJobs) {
            // Queue status stops the next timer tick from sending the same post again.
            job.queued();
            jobs.save(job);
            producer.send(job.getId());
        }
    }
}
