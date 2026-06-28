package com.aegis.backend.service;

import com.aegis.backend.ai.EmbeddingService;
import com.aegis.backend.entity.Document;
import com.aegis.backend.entity.DocumentChunk;
import com.aegis.backend.rag.ChunkingUtil;
import com.aegis.backend.repository.DocumentChunkRepository;
import com.aegis.backend.repository.DocumentRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ChunkingUtil chunkingUtil;
    private final EmbeddingService embeddingService;

    public DocumentServiceImpl(
            final DocumentRepository documentRepository,
            final DocumentChunkRepository documentChunkRepository,
            final ChunkingUtil chunkingUtil,
            final EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.chunkingUtil = chunkingUtil;
        this.embeddingService = embeddingService;
    }

    @Override
    @Transactional
    public Document ingestDocument(final String title, final String content) {
        log.info("Ingesting document title: '{}'", title);

        final Document document =
                Document.builder().title(title).content(content).build();

        final Document savedDocument = documentRepository.save(document);

        final List<String> chunks = chunkingUtil.splitIntoChunks(content, 500, 50);
        log.info("Split document content into {} chunks. Generating embeddings...", chunks.size());

        for (final String chunkText : chunks) {
            final float[] embedding = embeddingService.generateEmbedding(chunkText);

            final DocumentChunk chunkEntity = DocumentChunk.builder()
                    .document(savedDocument)
                    .content(chunkText)
                    .embedding(embedding)
                    .build();

            documentChunkRepository.save(chunkEntity);
        }

        log.info("Successfully ingested document and all its chunks.");
        return savedDocument;
    }
}
