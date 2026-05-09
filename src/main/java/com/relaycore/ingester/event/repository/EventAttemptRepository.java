package com.relaycore.ingester.event.repository;

import com.relaycore.ingester.event.domain.EventAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventAttemptRepository extends JpaRepository<EventAttempt, UUID> {
    List<EventAttempt> findByEventId(UUID eventId);
}
