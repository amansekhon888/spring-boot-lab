package com.example.demo.publishing;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Sends one small JSON message to Kafka. The listener below shows how it is received. */
@Component
public class PublishingProducer {
    public static final String TOPIC = "publishing.jobs";

    private final KafkaTemplate<Object, Object> kafka;

    public PublishingProducer(KafkaTemplate<Object, Object> kafka) {
        this.kafka = kafka;
    }

    public void send(Long publicationId) {
        // KafkaTemplate handles the network call; the id is also the message key.
        kafka.send(TOPIC, publicationId.toString(), new PublishEvent(publicationId));
    }
}
