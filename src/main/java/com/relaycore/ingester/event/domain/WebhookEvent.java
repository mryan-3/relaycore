package com.relaycore.ingester.event.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhook_events", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source", "external_event_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    private UUID id;

    @NotNull
    private String source;

    @NotNull
    @Column(name = "external_event_id")
    private String externalEventId;

    @NotNull
    @Column(name = "event_type")
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    private String signature;

    @NotNull
    @Column(name = "received_at", updatable = false)
    @Builder.Default
    private OffsetDateTime receivedAt = OffsetDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventStatus status = EventStatus.RECEIVED;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
