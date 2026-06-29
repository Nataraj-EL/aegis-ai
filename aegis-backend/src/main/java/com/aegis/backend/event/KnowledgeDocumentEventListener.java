package com.aegis.backend.event;

import com.aegis.backend.ai.EmbeddingService;
import com.aegis.backend.entity.KnowledgeDocument;
import com.aegis.backend.entity.KnowledgeEmbedding;
import com.aegis.backend.repository.KnowledgeDocumentRepository;
import com.aegis.backend.repository.KnowledgeEmbeddingRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class KnowledgeDocumentEventListener {

    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeEmbeddingRepository embeddingRepository;

    public KnowledgeDocumentEventListener(
            final EmbeddingService embeddingService,
            final KnowledgeDocumentRepository documentRepository,
            final KnowledgeEmbeddingRepository embeddingRepository) {
        this.embeddingService = embeddingService;
        this.documentRepository = documentRepository;
        this.embeddingRepository = embeddingRepository;
    }

    @Async
    @EventListener
    @Transactional
    public void handleKnowledgeDocumentSaved(final KnowledgeDocumentSavedEvent event) {
        log.info("Processing asynchronous embedding generation for KnowledgeDocument ID: {}", event.getDocumentId());
        try {
            final Optional<KnowledgeDocument> documentOpt = documentRepository.findById(event.getDocumentId());
            if (documentOpt.isEmpty()) {
                log.warn(
                        "KnowledgeDocument ID: {} no longer exists. Aborting embedding generation.",
                        event.getDocumentId());
                return;
            }
            final KnowledgeDocument document = documentOpt.get();

            // Invoke OCP embedding provider service
            final float[] embeddingVector = embeddingService.generateEmbedding(event.getContent());

            final KnowledgeEmbedding embedding = KnowledgeEmbedding.builder()
                    .documentId(document.getId())
                    .document(document)
                    .embedding(embeddingVector)
                    .modelVersion("text-embedding-004")
                    .build();

            embeddingRepository.save(embedding);
            log.info(
                    "Successfully generated and persisted embedding mapping for KnowledgeDocument ID: {}",
                    document.getId());
        } catch (final Exception exception) {
            log.error(
                    "Failed to generate or persist embedding mapping for KnowledgeDocument ID: {}",
                    event.getDocumentId(),
                    exception);
            // Graceful fallback - leaves embedding missing/null, downstream search handles this gracefully
        }
    }
}
