package com.relaycore.ingester.event.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_attempts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventAttempt {

    @Id
    private UUID id;

    @NotNull
    @Column(name = "event_id")
    private UUID eventId;

    @NotNull
    @Column(name = "attempt_number")
    private int attemptNumber;

    @NotNull
    @Column(name = "started_at")
    @Builder.Default
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @NotNull
    private String status; // SUCCESS / FAILURE

    @Column(name = "error_message")
    private String errorMessage;
}
