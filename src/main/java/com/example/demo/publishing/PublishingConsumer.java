package com.example.demo.publishing;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.stereotype.Component;

@Component
public class PublishingConsumer {
    private final PublishingService service;

    public PublishingConsumer(PublishingService s) {
        service = s;
    }

    @RetryableTopic(attempts = "4", backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 30000), include = RuntimeException.class, dltTopicSuffix = ".DLT")
    @KafkaListener(topics = "publishing.jobs", groupId = "social-publisher")
    public void consume(PublishEvent event) {
        service.publish(event);
    }
}
