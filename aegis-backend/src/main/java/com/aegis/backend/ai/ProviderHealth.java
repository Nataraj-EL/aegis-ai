package com.aegis.backend.ai;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProviderHealth {
    String status; // "UP" or "DOWN"
    String message;
    long latencyMs;
    long timestamp;
}
