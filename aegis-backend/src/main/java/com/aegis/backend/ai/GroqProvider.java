package com.aegis.backend.ai;

import com.aegis.backend.config.AiProperties;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ai.providers.groq", name = "api-key")
public class GroqProvider implements AiProvider {

    private final OpenAiClient openAiClient;
    private final AiProperties aiProperties;
    private ProviderHealth cachedHealth;

    public GroqProvider(final OpenAiClient openAiClient, final AiProperties aiProperties) {
        this.openAiClient = openAiClient;
        this.aiProperties = aiProperties;
    }

    @Override
    public String chat(final String systemPrompt, final String userMessage) {
        return openAiClient.chat(getConfig(), getProviderName(), systemPrompt, userMessage);
    }

    @Override
    public float[] generateEmbedding(final String text) {
        throw new UnsupportedOperationException("Embeddings not supported by Groq provider");
    }

    @Override
    public String getProviderName() {
        return "groq";
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
        return List.of(AiProviderCapability.CHAT);
    }

    private AiProperties.ProviderConfig getConfig() {
        return aiProperties.getProviders().get("groq");
    }
}
