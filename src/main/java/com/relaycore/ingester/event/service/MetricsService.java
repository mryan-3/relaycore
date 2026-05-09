package com.relaycore.ingester.event.service;

import com.relaycore.ingester.event.domain.EventStatus;
import com.relaycore.ingester.event.dto.MetricsResponse;
import com.relaycore.ingester.event.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final WebhookEventRepository eventRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String QUEUE_KEY = "webhook_events_queue";

    public MetricsResponse getMetrics() {
        long totalReceived = eventRepository.count();
        long totalProcessed = eventRepository.countByStatus(EventStatus.PROCESSED);
        long totalFailed = eventRepository.countByStatus(EventStatus.FAILED);

        Long queueDepth = redisTemplate.opsForList().size(QUEUE_KEY);

        // Simulating rate and time calculations for now
        // In a real prod environment, these would be calculated via native SQL or
        // Micrometer
        double processingRate = calculateRate();
        double avgTime = calculateAvgTime();

        return MetricsResponse.builder()
                .totalReceived(totalReceived)
                .totalProcessed(totalProcessed)
                .totalFailed(totalFailed)
                .queueDepth(queueDepth != null ? queueDepth : 0)
                .processingRatePerMin(processingRate)
                .averageProcessingTimeMs(avgTime)
                .build();
    }

    private double calculateRate() {
        // Mock logic: 100 events/min
        return 100.0;
    }

    private double calculateAvgTime() {
        // Mock logic: 250ms
        return 250.0;
    }
}
