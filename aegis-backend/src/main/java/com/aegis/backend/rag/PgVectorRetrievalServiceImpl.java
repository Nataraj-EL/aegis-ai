package com.aegis.backend.rag;

import com.aegis.backend.ai.EmbeddingService;
import com.aegis.backend.repository.DocumentChunkRepository;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PgVectorRetrievalServiceImpl implements RetrievalService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    public PgVectorRetrievalServiceImpl(
            final DocumentChunkRepository documentChunkRepository, final EmbeddingService embeddingService) {
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
    }

    @Override
    public List<String> retrieveContext(final String query, final int maxResults) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        try {
            if (documentChunkRepository.count() == 0) {
                log.info("No documents are ingested in the database yet. Skipping similarity query.");
                return Collections.emptyList();
            }

            log.info("Generating query embedding for retrieval search query: {}", query);
            final float[] queryVector = embeddingService.generateEmbedding(query);

            final String vectorString = toVectorString(queryVector);
            log.info("Executing pgvector cosine search similarity query...");
            return documentChunkRepository.findSimilarContent(vectorString, maxResults);
        } catch (final Exception exception) {
            log.error("Failed to retrieve context chunks from database using pgvector", exception);
            return Collections.emptyList();
        }
    }

    private String toVectorString(final float[] vector) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < vector.length; i++) {
            builder.append(vector[i]);
            if (i < vector.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
