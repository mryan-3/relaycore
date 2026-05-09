package com.relaycore.ingester.queue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisEventQueue implements EventQueue {

    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_KEY = "webhook_events_queue";

    @Override
    public void enqueue(UUID eventId) {
        redisTemplate.opsForList().rightPush(QUEUE_KEY, eventId.toString());
    }
}
