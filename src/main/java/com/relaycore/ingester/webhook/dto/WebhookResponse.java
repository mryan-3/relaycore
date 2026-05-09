package com.relaycore.ingester.webhook.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebhookResponse {
    private final String status;
    private final String eventId;
}
