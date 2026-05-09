package com.relaycore.ingester.event.dto;

import com.relaycore.ingester.event.domain.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class EventDetailResponse {
    private final UUID id;
    private final String source;
    private final String externalEventId;
    private final String eventType;
    private final String payload;
    private final EventStatus status;
    private final int retryCount;
    private final String lastError;
    private final OffsetDateTime receivedAt;
    private final OffsetDateTime processedAt;
    private final List<AttemptResponse> attempts;

    @Getter
    @Builder
    public static class AttemptResponse {
        private final UUID id;
        private final int attemptNumber;
        private final OffsetDateTime startedAt;
        private final OffsetDateTime finishedAt;
        private final String status;
        private final String errorMessage;
    }
}
