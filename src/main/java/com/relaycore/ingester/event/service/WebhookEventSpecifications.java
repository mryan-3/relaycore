package com.relaycore.ingester.event.service;

import com.relaycore.ingester.event.domain.EventStatus;
import com.relaycore.ingester.event.domain.WebhookEvent;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class WebhookEventSpecifications {

    public static Specification<WebhookEvent> withFilters(
            EventStatus status,
            String source,
            String eventType,
            OffsetDateTime from,
            OffsetDateTime to) {
        
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (source != null && !source.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("source"), source));
            }
            if (eventType != null && !eventType.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("eventType"), eventType));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("receivedAt"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("receivedAt"), to));
            }

            return predicate;
        };
    }
}
