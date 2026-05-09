package com.relaycore.ingester.event.repository;

import com.relaycore.ingester.event.domain.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    Optional<WebhookEvent> findBySourceAndExternalEventId(String source, String externalEventId);
}
