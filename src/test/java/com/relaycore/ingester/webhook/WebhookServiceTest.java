package com.relaycore.ingester.webhook;

import com.relaycore.ingester.event.domain.EventStatus;
import com.relaycore.ingester.event.domain.WebhookEvent;
import com.relaycore.ingester.event.repository.WebhookEventRepository;
import com.relaycore.ingester.queue.service.EventQueue;
import com.relaycore.ingester.webhook.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookEventRepository eventRepository;

    @Mock
    private EventQueue eventQueue;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void shouldPersistAndQueueNewEvent() {
        String source = "stripe";
        String externalId = "evt_123";
        when(eventRepository.findBySourceAndExternalEventId(source, externalId)).thenReturn(Optional.empty());
        when(eventRepository.save(any(WebhookEvent.class))).thenAnswer(i -> i.getArguments()[0]);

        UUID result = webhookService.acceptEvent(source, externalId, "type", "{}", "sig");

        assertThat(result).isNotNull();
        verify(eventRepository).save(any(WebhookEvent.class));
        verify(eventQueue).enqueue(any(UUID.class));
    }

    @Test
    void shouldReturnExistingIdForDuplicateEvent() {
        UUID existingId = UUID.randomUUID();
        WebhookEvent existingEvent = WebhookEvent.builder().id(existingId).build();
        when(eventRepository.findBySourceAndExternalEventId(anyString(), anyString()))
                .thenReturn(Optional.of(existingEvent));

        UUID result = webhookService.acceptEvent("stripe", "evt_123", "type", "{}", "sig");

        assertThat(result).isEqualTo(existingId);
        verify(eventRepository, never()).save(any());
        verify(eventQueue, never()).enqueue(any());
    }
}
