package com.example.demo.publishing;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import java.time.Duration;

class PublishingSchedulerTest {
    @Test void pollClaimsOnlyABoundedBatchWithARecoveryLease() {
        PublishingService service = mock(PublishingService.class);
        new PublishingScheduler(service).poll();
        verify(service).enqueueDue(25, Duration.ofMinutes(2));
        verifyNoMoreInteractions(service);
    }
}
