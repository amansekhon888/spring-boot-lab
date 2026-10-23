package com.example.demo.publishing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PublicationRepository extends JpaRepository<ScheduledPublication, Long> {
    // Spring Data builds this query from the method name: status and due time, oldest first.
    List<ScheduledPublication> findByStatusAndScheduledAtBeforeOrderByScheduledAt(
            PublishStatus status, Instant now, Pageable page);
}
