package com.aegis.backend.ai;

import java.util.List;

public interface AiProvider {
    String chat(String systemPrompt, String userMessage);

    float[] generateEmbedding(String text);

    String getProviderName();

    ProviderHealth healthCheck();

    List<AiProviderCapability> getCapabilities();
}
