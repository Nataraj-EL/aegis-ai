package com.aegis.backend.ai;

import com.aegis.backend.config.AiProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@SuppressWarnings("rawtypes")
public class OpenAiClient {

    public String chat(
            final AiProperties.ProviderConfig config,
            final String providerName,
            final String systemPrompt,
            final String userMessage) {

        final RestClient restClient = createRestClient(config);

        final List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        final Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        try {
            final Map response = restClient
                    .post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                final List choices = (List) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    final Map choice = (Map) choices.get(0);
                    final Map message = (Map) choice.get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            throw new IllegalStateException("Invalid response structure from provider: " + providerName);
        } catch (final HttpClientErrorException exception) {
            log.error("HTTP error calling AI provider {}: {}", providerName, exception.getStatusCode());
            throw exception;
        } catch (final Exception exception) {
            log.error("Error calling AI provider {}: {}", providerName, exception.getMessage());
            throw new IllegalStateException("AI provider call failed: " + providerName, exception);
        }
    }

    public float[] generateEmbedding(
            final AiProperties.ProviderConfig config, final String providerName, final String text) {

        final RestClient restClient = createRestClient(config);

        final Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("input", text);

        try {
            final Map response = restClient
                    .post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                final List data = (List) response.get("data");
                if (data != null && !data.isEmpty()) {
                    final Map dataItem = (Map) data.get(0);
                    final List embedding = (List) dataItem.get("embedding");
                    if (embedding != null) {
                        final float[] result = new float[embedding.size()];
                        for (int i = 0; i < embedding.size(); i++) {
                            result[i] = ((Number) embedding.get(i)).floatValue();
                        }
                        return result;
                    }
                }
            }
            throw new IllegalStateException("Invalid embedding response from provider: " + providerName);
        } catch (final HttpClientErrorException exception) {
            log.error("HTTP error generating embedding from provider {}: {}", providerName, exception.getStatusCode());
            throw exception;
        } catch (final Exception exception) {
            log.error("Error generating embedding from provider {}: {}", providerName, exception.getMessage());
            throw new IllegalStateException("Embedding generation failed: " + providerName, exception);
        }
    }

    public ProviderHealth healthCheck(final AiProperties.ProviderConfig config, final String providerName) {
        if (config == null
                || config.getBaseUrl() == null
                || config.getBaseUrl().trim().isEmpty()) {
            return ProviderHealth.builder()
                    .status("DOWN")
                    .message("Provider configuration is missing or incomplete")
                    .latencyMs(0L)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        final long startTime = System.currentTimeMillis();
        try {
            final RestClient.Builder builder = RestClient.builder().baseUrl(config.getBaseUrl());
            if (config.getApiKey() != null && !config.getApiKey().trim().isEmpty()) {
                builder.defaultHeader("Authorization", "Bearer " + config.getApiKey());
            }
            final RestClient restClient = builder.build();

            // Try loading models as a lightweight ping
            restClient.get().uri("/models").retrieve().toBodilessEntity();

            final long duration = System.currentTimeMillis() - startTime;
            return ProviderHealth.builder()
                    .status("UP")
                    .message("Connection successful")
                    .latencyMs(duration)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (final HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden exception) {
            final long duration = System.currentTimeMillis() - startTime;
            return ProviderHealth.builder()
                    .status("DOWN")
                    .message("Authentication failed: " + exception.getMessage())
                    .latencyMs(duration)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (final Exception exception) {
            return ProviderHealth.builder()
                    .status("DOWN")
                    .message("Connection failed: " + exception.getMessage())
                    .latencyMs(0L)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    private RestClient createRestClient(final AiProperties.ProviderConfig config) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(config.getTimeoutMs());
        requestFactory.setReadTimeout(config.getTimeoutMs());

        final RestClient.Builder builder =
                RestClient.builder().baseUrl(config.getBaseUrl()).requestFactory(requestFactory);
        if (config.getApiKey() != null && !config.getApiKey().trim().isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + config.getApiKey());
        }
        return builder.build();
    }
}
