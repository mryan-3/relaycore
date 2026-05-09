package com.relaycore.ingester.worker.service;

import com.relaycore.ingester.event.domain.EventAttempt;
import com.relaycore.ingester.event.domain.EventStatus;
import com.relaycore.ingester.event.domain.WebhookEvent;
import com.relaycore.ingester.event.repository.EventAttemptRepository;
import com.relaycore.ingester.event.repository.WebhookEventRepository;
import com.relaycore.ingester.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProcessor {

    private final WebhookEventRepository eventRepository;
    private final EventAttemptRepository attemptRepository;

    @Transactional
    public void processEvent(UUID eventId) {
        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (event.getStatus() == EventStatus.PROCESSED) {
            log.info("Event {} already processed. Skipping.", eventId);
            return;
        }

        event.setStatus(EventStatus.PROCESSING);
        eventRepository.save(event);

        EventAttempt attempt = EventAttempt.builder()
                .id(IdGenerator.nextId())
                .eventId(eventId)
                .attemptNumber(event.getRetryCount() + 1)
                .startedAt(OffsetDateTime.now())
                .status("STARTED")
                .build();
        attemptRepository.save(attempt);

        try {
            simulateBusinessLogic(event);
            
            event.setStatus(EventStatus.PROCESSED);
            event.setProcessedAt(OffsetDateTime.now());
            attempt.setStatus("SUCCESS");
            log.info("Event {} processed successfully.", eventId);
        } catch (Exception e) {
            log.error("Failed to process event {}: {}", eventId, e.getMessage());
            event.setStatus(EventStatus.FAILED);
            event.setLastError(e.getMessage());
            event.incrementRetryCount();
            
            attempt.setStatus("FAILURE");
            attempt.setErrorMessage(e.getMessage());
            
            throw e; // Rethrow to trigger retry logic in the worker
        } finally {
            attempt.setFinishedAt(OffsetDateTime.now());
            eventRepository.save(event);
            attemptRepository.save(attempt);
        }
    }

    private void simulateBusinessLogic(WebhookEvent event) {
        log.info("Executing business logic for event: {} (Type: {})", event.getId(), event.getEventType());
        // Simulating work...
        if (event.getPayload().contains("error")) {
            throw new RuntimeException("Simulated processing error for payload");
        }
    }
}
