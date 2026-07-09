package com.aegis.backend.ai;

import com.aegis.backend.config.AiProperties;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ai.providers.openai", name = "api-key")
public class OpenAiProvider implements AiProvider {

    private final OpenAiClient openAiClient;
    private final AiProperties aiProperties;
    private ProviderHealth cachedHealth;

    public OpenAiProvider(final OpenAiClient openAiClient, final AiProperties aiProperties) {
        this.openAiClient = openAiClient;
        this.aiProperties = aiProperties;
    }

    @Override
    public String chat(final String systemPrompt, final String userMessage) {
        return openAiClient.chat(getConfig(), getProviderName(), systemPrompt, userMessage);
    }

    @Override
    public float[] generateEmbedding(final String text) {
        return openAiClient.generateEmbedding(getConfig(), getProviderName(), text);
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public ProviderHealth healthCheck() {
        if (cachedHealth != null && (System.currentTimeMillis() - cachedHealth.getTimestamp() < 30_000)) {
            return cachedHealth;
        }
        cachedHealth = openAiClient.healthCheck(getConfig(), getProviderName());
        return cachedHealth;
    }

    @Override
    public List<AiProviderCapability> getCapabilities() {
        return List.of(AiProviderCapability.CHAT, AiProviderCapability.EMBEDDING);
    }

    private AiProperties.ProviderConfig getConfig() {
        return aiProperties.getProviders().get("openai");
    }
}
