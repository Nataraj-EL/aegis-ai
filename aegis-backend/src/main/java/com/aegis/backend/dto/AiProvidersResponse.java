package com.aegis.backend.dto;

import com.aegis.backend.ai.AiProviderCapability;
import com.aegis.backend.ai.ProviderHealth;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AiProvidersResponse {

    String activeProvider;
    List<ProviderInfo> providers;

    @Value
    @Builder
    public static class ProviderInfo {
        String name;
        String model;
        List<AiProviderCapability> capabilities;
        ProviderHealth health;
    }
}
