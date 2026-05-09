package com.relaycore.ingester.event.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetricsResponse {
    private final long totalReceived;
    private final long totalProcessed;
    private final long totalFailed;
    private final long queueDepth;
    private final double processingRatePerMin;
    private final double averageProcessingTimeMs;
}
