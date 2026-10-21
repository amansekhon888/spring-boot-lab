package com.example.demo.publishing;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;

public interface PublicationRepository extends JpaRepository<ScheduledPublication, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ScheduledPublication p where p.status in :statuses and p.scheduledAt <= :now order by p.scheduledAt")
    List<ScheduledPublication> lockDue(@Param("statuses") List<PublishStatus> statuses, @Param("now") Instant now,
            org.springframework.data.domain.Pageable page);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ScheduledPublication p where p.status = :status and p.leaseUntil < :now order by p.leaseUntil")
    List<ScheduledPublication> lockExpired(@Param("status") PublishStatus status, @Param("now") Instant now,
            org.springframework.data.domain.Pageable page);
}
