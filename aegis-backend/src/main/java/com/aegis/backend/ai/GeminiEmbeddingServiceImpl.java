package com.aegis.backend.ai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class GeminiEmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public GeminiEmbeddingServiceImpl(final EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] generateEmbedding(final String text) {
        return embeddingModel.embed(text);
    }
}
