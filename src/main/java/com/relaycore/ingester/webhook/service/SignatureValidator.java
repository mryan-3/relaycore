package com.relaycore.ingester.webhook.service;

public interface SignatureValidator {
    boolean isValid(String source, String payload, String signature);
}
