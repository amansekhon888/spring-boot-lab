package com.example.demo.publishing;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PublishingConsumerTest {
    @Test void publishesQueuedPostAndSavesItsStatus() {
        var jobs = mock(PublicationRepository.class);
        var publisher = mock(SocialMediaPublisher.class);
        var post = new ScheduledPublication(Platform.X, "Hello", Instant.now());
        when(jobs.findById(7L)).thenReturn(Optional.of(post));
        when(publisher.publish(Platform.X, "Hello")).thenReturn("mock:42");

        new PublishingConsumer(jobs, publisher).consume(new PublishEvent(7L));

        assertEquals(PublishStatus.PUBLISHED, post.getStatus());
        verify(jobs).save(post);
    }

    @Test void skipsEventIfPostIsAlreadyPublished() {
        var jobs = mock(PublicationRepository.class);
        var publisher = mock(SocialMediaPublisher.class);
        var post = new ScheduledPublication(Platform.X, "Hello", Instant.now());
        post.published("mock:already-done");
        when(jobs.findById(7L)).thenReturn(Optional.of(post));

        new PublishingConsumer(jobs, publisher).consume(new PublishEvent(7L));

        verifyNoInteractions(publisher);
    }
}
