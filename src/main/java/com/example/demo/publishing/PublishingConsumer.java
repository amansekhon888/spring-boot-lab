package com.example.demo.publishing;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PublishingConsumer {
    private final PublicationRepository jobs;
    private final SocialMediaPublisher publisher;

    public PublishingConsumer(PublicationRepository jobs, SocialMediaPublisher publisher) {
        this.jobs = jobs;
        this.publisher = publisher;
    }

    @KafkaListener(topics = "publishing.jobs", groupId = "social-publisher")
    public void consume(PublishEvent event) {
        var job = jobs.findById(event.publicationId()).orElseThrow();
        // Kafka may deliver an event again; do not publish a post that already succeeded.
        if (job.getStatus() == PublishStatus.PUBLISHED) return;
        // Throwing lets Spring Kafka retry, then route the event to the dead-letter topic.
        job.published(publisher.publish(job.getPlatform(), job.getContent()));
        jobs.save(job);
    }
}
