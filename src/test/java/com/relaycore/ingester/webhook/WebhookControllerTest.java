package com.relaycore.ingester.webhook;

import com.relaycore.ingester.webhook.controller.WebhookController;
import com.relaycore.ingester.webhook.service.SignatureValidator;
import com.relaycore.ingester.webhook.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookService webhookService;

    @MockBean
    private SignatureValidator signatureValidator;

    @Test
    void shouldAcceptWebhookWhenValid() throws Exception {
        UUID internalId = UUID.randomUUID();
        when(signatureValidator.isValid(anyString(), anyString(), anyString())).thenReturn(true);
        when(webhookService.acceptEvent(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(internalId);

        mockMvc.perform(post("/webhooks/stripe")
                        .header("X-Signature", "valid-sig")
                        .header("X-Event-ID", "evt_123")
                        .header("X-Event-Type", "payment.succeeded")
                        .content("{\"amount\": 1000}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.event_id").value(internalId.toString()));
    }

    @Test
    void shouldReturnBadRequestWhenSignatureIsInvalid() throws Exception {
        when(signatureValidator.isValid(anyString(), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/webhooks/stripe")
                        .header("X-Signature", "invalid-sig")
                        .header("X-Event-ID", "evt_123")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
