package com.aegis.backend.memory;

import java.util.List;

public interface VectorMemory extends Memory {
    void saveEmbedding(String sessionId, String text, List<Double> embedding);

    List<String> findSimilar(String sessionId, List<Double> queryEmbedding, int maxResults);
}
