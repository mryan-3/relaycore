package com.relaycore.ingester.webhook.service;

import com.relaycore.ingester.event.domain.EventStatus;
import com.relaycore.ingester.event.domain.WebhookEvent;
import com.relaycore.ingester.event.repository.WebhookEventRepository;
import com.relaycore.ingester.queue.service.EventQueue;
import com.relaycore.ingester.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookEventRepository eventRepository;
    private final EventQueue eventQueue;

    @Transactional
    public UUID acceptEvent(String source, String externalId, String type, String payload, String signature) {
        Optional<WebhookEvent> existingEvent = eventRepository.findBySourceAndExternalEventId(source, externalId);

        if (existingEvent.isPresent()) {
            log.info("Duplicate event detected: source={}, external_id={}. Skipping persistence and queueing.", source, externalId);
            return existingEvent.get().getId();
        }

        WebhookEvent event = WebhookEvent.builder()
                .id(IdGenerator.nextId())
                .source(source)
                .externalEventId(externalId)
                .eventType(type)
                .payload(payload)
                .signature(signature)
                .status(EventStatus.RECEIVED)
                .build();

        WebhookEvent savedEvent = eventRepository.save(event);
        log.info("Event received and persisted: id={}, source={}, external_id={}", savedEvent.getId(), source, externalId);

        eventQueue.enqueue(savedEvent.getId());
        log.debug("Event pushed to queue: id={}", savedEvent.getId());

        return savedEvent.getId();
    }
}
