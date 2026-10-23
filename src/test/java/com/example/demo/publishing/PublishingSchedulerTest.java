package com.example.demo.publishing;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PublishingSchedulerTest {
    @Test void sendsDueScheduledPostsToKafkaAndMarksThemQueued() {
        var repository = mock(PublicationRepository.class);
        var producer = mock(PublishingProducer.class);
        var post = new ScheduledPublication(Platform.X, "Classroom post", Instant.now().minusSeconds(5));
        when(repository.findByStatusAndScheduledAtBeforeOrderByScheduledAt(
                eq(PublishStatus.SCHEDULED), any(Instant.class), any(Pageable.class))).thenReturn(List.of(post));

        new PublishingScheduler(repository, producer).poll();

        verify(repository).save(post);
        verify(producer).send(post.getId());
        org.junit.jupiter.api.Assertions.assertEquals(PublishStatus.QUEUED, post.getStatus());
    }
}
