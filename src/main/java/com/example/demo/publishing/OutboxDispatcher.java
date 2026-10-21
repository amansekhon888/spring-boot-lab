package com.example.demo.publishing;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * At-least-once relay. A crash after Kafka accepts but before markSent can
 * republish; consumer idempotency handles it.
 */
@Component
public class OutboxDispatcher {
    private final OutboxRepository repository;
    private final KafkaTemplate<Object, Object> kafka;

    public OutboxDispatcher(OutboxRepository r, KafkaTemplate<Object, Object> k) {
        repository = r;
        kafka = k;
    }

    @Scheduled(fixedDelayString = "${app.publishing.outbox-ms:1000}")
    @Transactional
    public void dispatch() {
        for (var row : repository.findTop50BySentAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafka.send("publishing.jobs", row.getId().toString(),
                        new PublishEvent(row.getId(), row.getPublicationId())).get();
                row.markSent();
            } catch (Exception ex) {
                return;
            }
        }
    }
}
