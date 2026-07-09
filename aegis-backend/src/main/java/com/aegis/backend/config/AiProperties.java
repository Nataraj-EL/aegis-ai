package com.aegis.backend.config;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String provider = "gemini";
    private FallbackStrategy fallbackStrategy = FallbackStrategy.FAILOVER;
    private List<String> priorityList;
    private Map<String, ProviderConfig> providers;

    public List<String> getFallbackChain() {
        final List<String> fallbackChain = new java.util.ArrayList<>();
        final String primary = getProvider();
        final FallbackStrategy strategy = getFallbackStrategy();

        if (strategy == FallbackStrategy.PRIMARY_ONLY) {
            fallbackChain.add(primary);
        } else if (strategy == FallbackStrategy.FAILOVER) {
            fallbackChain.add(primary);
            if (getPriorityList() != null) {
                for (final String providerName : getPriorityList()) {
                    if (!fallbackChain.contains(providerName)) {
                        fallbackChain.add(providerName);
                    }
                }
            }
        } else if (strategy == FallbackStrategy.PRIORITY_CHAIN) {
            if (getPriorityList() != null) {
                for (final String providerName : getPriorityList()) {
                    if (!fallbackChain.contains(providerName)) {
                        fallbackChain.add(providerName);
                    }
                }
            }
            if (!fallbackChain.contains(primary)) {
                fallbackChain.add(primary);
            }
        }
        return fallbackChain;
    }

    @Data
    public static class ProviderConfig {
        @ToString.Exclude
        private String apiKey;

        private String baseUrl;
        private String model;
        private int timeoutMs = 10_000;
        private int retries = 3;
    }
}
