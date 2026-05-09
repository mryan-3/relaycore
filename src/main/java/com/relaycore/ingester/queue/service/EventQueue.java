package com.relaycore.ingester.queue.service;

import java.util.UUID;

public interface EventQueue {
    void enqueue(UUID eventId);
}
