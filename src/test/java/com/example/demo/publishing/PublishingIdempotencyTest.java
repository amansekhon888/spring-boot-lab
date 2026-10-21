package com.example.demo.publishing;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.List;
import static org.mockito.Mockito.*;

class PublishingIdempotencyTest {
    @Test void alreadyProcessedEventDoesNotCallPlatformAgain() {
        var publications = mock(PublicationRepository.class);
        var outbox = mock(OutboxRepository.class);
        var processed = mock(ProcessedEventRepository.class);
        var publisher = mock(SocialMediaPublisher.class);
        when(processed.existsById(any())).thenReturn(true);
        var service = new PublishingService(publications, outbox, processed, List.of(publisher));

        service.publish(new PublishEvent(UUID.randomUUID(), 99L));

        verifyNoInteractions(publications, publisher);
        verify(processed).existsById(any());
    }
}
