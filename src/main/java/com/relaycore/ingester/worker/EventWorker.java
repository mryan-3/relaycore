package com.relaycore.ingester.worker;

import com.relaycore.ingester.event.domain.WebhookEvent;
import com.relaycore.ingester.event.repository.WebhookEventRepository;
import com.relaycore.ingester.worker.service.EventProcessor;
import com.relaycore.ingester.worker.service.RetryService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.worker.enabled", havingValue = "true", matchIfMissing = true)
public class EventWorker {

    private final StringRedisTemplate redisTemplate;
    private final EventProcessor eventProcessor;
    private final RetryService retryService;
    private final WebhookEventRepository eventRepository;

    private static final String QUEUE_KEY = "webhook_events_queue";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        executorService.submit(this::pollQueue);
        log.info("Background Event Worker started.");
    }

    private void pollQueue() {
        while (running) {
            try {
                // BRPOP equivalent in Spring Data Redis
                String eventIdStr = redisTemplate.opsForList().rightPop(QUEUE_KEY, 5, TimeUnit.SECONDS);

                if (eventIdStr != null) {
                    UUID eventId = UUID.fromString(eventIdStr);
                    log.debug("Picked up event {} from queue.", eventId);
                    process(eventId);
                }
            } catch (Exception e) {
                log.error("Error polling queue: {}", e.getMessage(), e);
                try {
                    Thread.sleep(1000); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void process(UUID eventId) {
        try {
            eventProcessor.processEvent(eventId);
        } catch (Exception e) {
            // Handle retry logic
            eventRepository.findById(eventId).ifPresent(event -> {
                retryService.scheduleRetry(eventId, event.getRetryCount());
            });
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        log.info("Background Event Worker stopped.");
    }
}
