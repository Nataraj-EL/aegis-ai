package com.aegis.backend.observability;

import com.aegis.backend.ai.AiProvider;
import com.aegis.backend.ai.ProviderHealth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AiProviderHealthIndicator implements HealthIndicator {

    private final List<AiProvider> providers;

    public AiProviderHealthIndicator(final List<AiProvider> providers) {
        this.providers = providers;
    }

    @Override
    public Health health() {
        boolean anyAvailable = false;
        final Map<String, Object> details = new HashMap<>();

        for (final AiProvider provider : providers) {
            final ProviderHealth health = provider.healthCheck();
            details.put(
                    provider.getProviderName(),
                    Map.of(
                            "status", health.getStatus(),
                            "message", health.getMessage(),
                            "latencyMs", health.getLatencyMs()));
            if ("UP".equals(health.getStatus())) {
                anyAvailable = true;
            }
        }

        if (anyAvailable) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down()
                    .withDetails(details)
                    .withDetail("error", "No AI providers are available")
                    .build();
        }
    }
}
