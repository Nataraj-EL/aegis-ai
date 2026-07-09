package com.aegis.backend.ai;

import com.aegis.backend.config.AiProperties;
import java.util.List;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ai.providers.gemini", name = "api-key")
public class GeminiProvider implements AiProvider {

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final AiProperties aiProperties;
    private ProviderHealth cachedHealth;

    public GeminiProvider(
            final ObjectProvider<ChatModel> chatModelProvider,
            final ObjectProvider<EmbeddingModel> embeddingModelProvider,
            final AiProperties aiProperties) {
        this.chatModelProvider = chatModelProvider;
        this.embeddingModelProvider = embeddingModelProvider;
        this.aiProperties = aiProperties;
    }

    @Override
    public String chat(final String systemPrompt, final String userMessage) {
        final ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            throw new IllegalStateException("Gemini ChatModel is not initialized");
        }
        final SystemMessage systemMsg = new SystemMessage(systemPrompt);
        final UserMessage userMsg = new UserMessage(userMessage);
        final Prompt prompt = new Prompt(List.of(systemMsg, userMsg));
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    @Override
    public float[] generateEmbedding(final String text) {
        final EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel == null) {
            throw new IllegalStateException("Gemini EmbeddingModel is not initialized");
        }
        return embeddingModel.embed(text);
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public ProviderHealth healthCheck() {
        if (cachedHealth != null && (System.currentTimeMillis() - cachedHealth.getTimestamp() < 30_000)) {
            return cachedHealth;
        }

        final long startTime = System.currentTimeMillis();
        final AiProperties.ProviderConfig config = aiProperties.getProviders().get("gemini");
        if (config == null
                || config.getApiKey() == null
                || config.getApiKey().trim().isEmpty()) {
            cachedHealth = ProviderHealth.builder()
                    .status("DOWN")
                    .message("Gemini API key is not configured")
                    .latencyMs(0L)
                    .timestamp(System.currentTimeMillis())
                    .build();
            return cachedHealth;
        }

        final ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            cachedHealth = ProviderHealth.builder()
                    .status("DOWN")
                    .message("Gemini ChatModel bean is not available in Spring context")
                    .latencyMs(0L)
                    .timestamp(System.currentTimeMillis())
                    .build();
            return cachedHealth;
        }

        final long duration = System.currentTimeMillis() - startTime;
        cachedHealth = ProviderHealth.builder()
                .status("UP")
                .message("Gemini provider is initialized and active")
                .latencyMs(duration)
                .timestamp(System.currentTimeMillis())
                .build();
        return cachedHealth;
    }

    @Override
    public List<AiProviderCapability> getCapabilities() {
        return List.of(AiProviderCapability.CHAT, AiProviderCapability.EMBEDDING);
    }
}
