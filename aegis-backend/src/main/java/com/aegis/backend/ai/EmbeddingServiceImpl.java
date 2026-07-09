package com.aegis.backend.ai;

import com.aegis.backend.config.AiProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private final Map<String, AiProvider> providers;
    private final AiProperties aiProperties;

    public EmbeddingServiceImpl(final List<AiProvider> providerList, final AiProperties aiProperties) {
        this.providers = providerList.stream().collect(Collectors.toMap(AiProvider::getProviderName, p -> p));
        this.aiProperties = aiProperties;
    }

    @Override
    public float[] generateEmbedding(final String text) {
        final List<String> fallbackChain = aiProperties.getFallbackChain();

        Exception lastException = null;
        for (final String providerName : fallbackChain) {
            final AiProvider provider = providers.get(providerName);
            if (provider == null) {
                continue;
            }

            if (!provider.getCapabilities().contains(AiProviderCapability.EMBEDDING)) {
                log.debug("AI Provider {} does not support embeddings. Skipping.", providerName);
                continue;
            }

            final ProviderHealth health = provider.healthCheck();
            if ("DOWN".equals(health.getStatus())) {
                log.warn("AI Provider {} is unhealthy. Skipping embedding generation.", providerName);
                continue;
            }

            try {
                final float[] embedding = provider.generateEmbedding(text);
                if (embedding != null) {
                    return embedding;
                }
            } catch (final Exception exception) {
                log.error("Embedding generation failed with provider {}: {}", providerName, exception.getMessage());
                lastException = exception;
            }
        }

        throw new IllegalStateException("All available AI Providers failed to generate embedding.", lastException);
    }
}
