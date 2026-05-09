package com.relaycore.ingester.event.dto;

import com.relaycore.ingester.event.domain.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventSummaryResponse {
    private final UUID id;
    private final String source;
    private final String eventType;
    private final EventStatus status;
    private final OffsetDateTime receivedAt;
}
