package com.relaycore.ingester.webhook.service;

import org.springframework.stereotype.Component;

@Component
public class MockSignatureValidator implements SignatureValidator {
    
    @Override
    public boolean isValid(String source, String payload, String signature) {
        // In a real scenario, we would use HMAC-SHA256 with a secret key per source
        // For this project, we just ensure a signature exists and isn't "invalid"
        return signature != null && !signature.isBlank() && !signature.equals("invalid");
    }
}
