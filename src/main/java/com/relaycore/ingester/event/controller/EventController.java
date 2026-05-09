package com.relaycore.ingester.event.controller;

import com.relaycore.ingester.event.domain.EventStatus;
import com.relaycore.ingester.event.dto.EventDetailResponse;
import com.relaycore.ingester.event.dto.EventSummaryResponse;
import com.relaycore.ingester.event.dto.PagedResponse;
import com.relaycore.ingester.event.dto.MetricsResponse;
import com.relaycore.ingester.event.service.EventQueryService;
import com.relaycore.ingester.event.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventQueryService queryService;
    private final MetricsService metricsService;

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventSummaryResponse>> getEvents(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String event_type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        var response = queryService.findEvents(status, source, event_type, from, to, page, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailResponse> getEvent(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.getEventDetail(id));
    }

    @PostMapping("/{id}/replay")
    public ResponseEntity<Map<String, String>> replayEvent(@PathVariable UUID id) {
        queryService.replayEvent(id);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Event " + id + " has been re-queued for processing"
        ));
    }
}
