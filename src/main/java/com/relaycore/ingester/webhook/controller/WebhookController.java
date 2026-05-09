package com.relaycore.ingester.webhook.controller;

import com.relaycore.ingester.webhook.dto.WebhookResponse;
import com.relaycore.ingester.webhook.service.SignatureValidator;
import com.relaycore.ingester.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;
    private final SignatureValidator signatureValidator;

    @PostMapping("/{source}")
    public ResponseEntity<WebhookResponse> handleWebhook(
            @PathVariable String source,
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Event-ID") String externalId,
            @RequestHeader(value = "X-Event-Type", defaultValue = "unknown") String type,
            @RequestBody String payload) {

        log.debug("Received webhook request: source={}, external_id={}, signature={}", source, externalId, signature);

        if (!signatureValidator.isValid(source, payload, signature)) {
            log.warn("Invalid signature for source={}", source);
            return ResponseEntity.badRequest().build();
        }

        UUID internalId = webhookService.acceptEvent(source, externalId, type, payload, signature);

        return ResponseEntity.ok(WebhookResponse.builder()
                .status("accepted")
                .eventId(internalId.toString())
                .build());
    }
}
