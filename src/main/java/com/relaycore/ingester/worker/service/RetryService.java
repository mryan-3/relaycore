package com.relaycore.ingester.worker.service;

import com.relaycore.ingester.queue.service.EventQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private final EventQueue eventQueue;
    private final TaskScheduler taskScheduler;

    private static final int[] RETRY_DELAYS_SECONDS = {10, 30, 120}; // 10s, 30s, 2m

    public void scheduleRetry(UUID eventId, int currentRetryCount) {
        if (currentRetryCount >= RETRY_DELAYS_SECONDS.length) {
            log.warn("Max retries reached for event {}. Marking as permanently failed.", eventId);
            return;
        }

        int delay = RETRY_DELAYS_SECONDS[currentRetryCount];
        log.info("Scheduling retry #{} for event {} in {} seconds.", currentRetryCount + 1, eventId, delay);

        taskScheduler.schedule(() -> {
            log.info("Executing scheduled retry for event {}.", eventId);
            eventQueue.enqueue(eventId);
        }, Instant.now().plusSeconds(delay));
    }
}
