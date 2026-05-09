package com.relaycore.ingester.event.service;

import com.relaycore.ingester.event.domain.EventStatus;
import com.relaycore.ingester.event.domain.WebhookEvent;
import com.relaycore.ingester.event.dto.EventDetailResponse;
import com.relaycore.ingester.event.dto.EventSummaryResponse;
import com.relaycore.ingester.event.dto.PagedResponse;
import com.relaycore.ingester.event.repository.EventAttemptRepository;
import com.relaycore.ingester.event.repository.WebhookEventRepository;
import com.relaycore.ingester.queue.service.EventQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventQueryService {

    private final WebhookEventRepository eventRepository;
    private final EventAttemptRepository attemptRepository;
    private final EventQueue eventQueue;

    public PagedResponse<EventSummaryResponse> findEvents(
            EventStatus status,
            String source,
            String eventType,
            OffsetDateTime from,
            OffsetDateTime to,
            int page,
            int limit) {

        var spec = WebhookEventSpecifications.withFilters(status, source, eventType, from, to);
        var pageable = PageRequest.of(page - 1, limit, Sort.by("receivedAt").descending());

        Page<WebhookEvent> eventPage = eventRepository.findAll(spec, pageable);

        var data = eventPage.getContent().stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());

        return PagedResponse.<EventSummaryResponse>builder()
                .status("success")
                .page(page)
                .limit(limit)
                .total(eventPage.getTotalElements())
                .data(data)
                .build();
    }

    public EventDetailResponse getEventDetail(UUID id) {
        WebhookEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        var attempts = attemptRepository.findByEventId(id).stream()
                .map(attempt -> EventDetailResponse.AttemptResponse.builder()
                        .id(attempt.getId())
                        .attemptNumber(attempt.getAttemptNumber())
                        .startedAt(attempt.getStartedAt())
                        .finishedAt(attempt.getFinishedAt())
                        .status(attempt.getStatus())
                        .errorMessage(attempt.getErrorMessage())
                        .build())
                .collect(Collectors.toList());

        return EventDetailResponse.builder()
                .id(event.getId())
                .source(event.getSource())
                .externalEventId(event.getExternalEventId())
                .eventType(event.getEventType())
                .payload(event.getPayload())
                .status(event.getStatus())
                .retryCount(event.getRetryCount())
                .lastError(event.getLastError())
                .receivedAt(event.getReceivedAt())
                .processedAt(event.getProcessedAt())
                .attempts(attempts)
                .build();
    }

    @Transactional
    public void replayEvent(UUID id) {
        WebhookEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != EventStatus.FAILED) {
            throw new RuntimeException("Only failed events can be replayed");
        }

        log.info("Replaying event: {}", id);
        event.setStatus(EventStatus.RECEIVED);
        event.setRetryCount(0);
        event.setLastError(null);
        eventRepository.save(event);

        eventQueue.enqueue(id);
    }

    private EventSummaryResponse toSummaryDto(WebhookEvent event) {
        return EventSummaryResponse.builder()
                .id(event.getId())
                .source(event.getSource())
                .eventType(event.getEventType())
                .status(event.getStatus())
                .receivedAt(event.getReceivedAt())
                .build();
    }
}
