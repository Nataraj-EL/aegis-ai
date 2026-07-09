package com.aegis.backend.ai;

import com.aegis.backend.config.AiProperties;
import com.aegis.backend.service.MetricsService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final Map<String, AiProvider> providers;
    private final AiProperties aiProperties;
    private final MetricsService metricsService;

    public AiServiceImpl(
            final List<AiProvider> providerList, final AiProperties aiProperties, final MetricsService metricsService) {
        this.providers = providerList.stream().collect(Collectors.toMap(AiProvider::getProviderName, p -> p));
        this.aiProperties = aiProperties;
        this.metricsService = metricsService;
    }

    @Override
    public String generateResponse(final String systemPrompt, final String userMessage) {
        final List<String> fallbackChain = aiProperties.getFallbackChain();

        Exception lastException = null;
        for (final String providerName : fallbackChain) {
            final AiProvider provider = providers.get(providerName);
            if (provider == null) {
                log.warn("AI Provider {} is not instantiated (missing API key/config). Skipping.", providerName);
                continue;
            }

            final ProviderHealth health = provider.healthCheck();
            if ("DOWN".equals(health.getStatus())) {
                log.warn("AI Provider {} health status is DOWN ({}). Skipping.", providerName, health.getMessage());
                continue;
            }

            log.info("Attempting AI request with provider: {}", providerName);
            int retries = 0;
            final AiProperties.ProviderConfig config =
                    aiProperties.getProviders().get(providerName);
            final int maxRetries = config != null ? config.getRetries() : 3;

            while (true) {
                final long startTime = System.currentTimeMillis();
                try {
                    final String response = provider.chat(systemPrompt, userMessage);
                    final long duration = System.currentTimeMillis() - startTime;
                    metricsService.recordAiProviderRequest(providerName, "success", duration);
                    return response;
                } catch (final HttpClientErrorException exception) {
                    final long duration = System.currentTimeMillis() - startTime;
                    metricsService.recordAiProviderRequest(providerName, "failure", duration);

                    final int statusCode = exception.getStatusCode().value();
                    if (statusCode == 400 || statusCode == 401 || statusCode == 403 || statusCode == 404) {
                        log.error(
                                "Non-transient error calling AI provider {}: {}. Skipping.",
                                providerName,
                                exception.getMessage());
                        lastException = exception;
                        break; // Break retry loop, move to next provider
                    }

                    if (retries < maxRetries) {
                        retries++;
                        log.warn(
                                "Transient HTTP error calling AI provider {}, retrying ({}/{}): {}",
                                providerName,
                                retries,
                                maxRetries,
                                exception.getMessage());
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        log.error(
                                "Failed calling AI provider {} after {} retries: {}",
                                providerName,
                                maxRetries,
                                exception.getMessage());
                        lastException = exception;
                        break;
                    }
                } catch (final Exception exception) {
                    final long duration = System.currentTimeMillis() - startTime;
                    metricsService.recordAiProviderRequest(providerName, "failure", duration);

                    if (retries < maxRetries) {
                        retries++;
                        log.warn(
                                "Error calling AI provider {}, retrying ({}/{}): {}",
                                providerName,
                                retries,
                                maxRetries,
                                exception.getMessage());
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        log.error(
                                "Failed calling AI provider {} after {} retries: {}",
                                providerName,
                                maxRetries,
                                exception.getMessage());
                        lastException = exception;
                        break;
                    }
                }
            }
        }

        throw new IllegalStateException("All AI Providers failed to respond.", lastException);
    }
}
