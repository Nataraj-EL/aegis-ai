package com.aegis.backend.service;

import com.aegis.backend.dto.KnowledgeCreateRequest;
import com.aegis.backend.dto.KnowledgeResponse;
import com.aegis.backend.entity.KnowledgeDocument;
import com.aegis.backend.entity.KnowledgeEmbedding;
import com.aegis.backend.entity.KnowledgeStatus;
import com.aegis.backend.event.KnowledgeDocumentSavedEvent;
import com.aegis.backend.repository.KnowledgeDocumentRepository;
import com.aegis.backend.repository.KnowledgeDocumentSpecifications;
import com.aegis.backend.repository.KnowledgeEmbeddingRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class KnowledgeDocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeEmbeddingRepository embeddingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public KnowledgeDocumentService(
            final KnowledgeDocumentRepository documentRepository,
            final KnowledgeEmbeddingRepository embeddingRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.documentRepository = documentRepository;
        this.embeddingRepository = embeddingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public KnowledgeResponse createDocument(final KnowledgeCreateRequest request) {
        final KnowledgeDocument document = KnowledgeDocument.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .source(request.getSource())
                .tags(request.getTags())
                .status(request.getStatus())
                .build();

        final KnowledgeDocument saved = documentRepository.save(document);
        log.info("Ingested new knowledge base document: {}", saved.getTitle());

        eventPublisher.publishEvent(new KnowledgeDocumentSavedEvent(this, saved.getId(), saved.getContent()));
        return mapToResponse(saved);
    }

    @Transactional
    public KnowledgeResponse updateDocument(final UUID id, final KnowledgeCreateRequest request) {
        final KnowledgeDocument document = documentRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Knowledge document not found"));

        final String previousContent = document.getContent();

        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setSource(request.getSource());
        document.setTags(request.getTags());
        document.setStatus(request.getStatus());

        final KnowledgeDocument saved = documentRepository.save(document);
        log.info("Updated knowledge base document: {}", saved.getTitle());

        // Re-generate embedding only if content has changed
        if (!request.getContent().equals(previousContent)) {
            eventPublisher.publishEvent(new KnowledgeDocumentSavedEvent(this, saved.getId(), saved.getContent()));
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDocument(final UUID id) {
        // Marks status as ARCHIVED (Soft delete)
        final KnowledgeDocument document = documentRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Knowledge document not found"));

        if (document.getStatus() == KnowledgeStatus.ARCHIVED) {
            log.info("Knowledge document is already archived/soft-deleted");
            return;
        }

        document.setStatus(KnowledgeStatus.ARCHIVED);
        documentRepository.save(document);
        log.info("Soft deleted (archived) knowledge base document: {}", document.getTitle());
    }

    @Transactional(readOnly = true)
    public List<KnowledgeResponse> getDocuments(
            final KnowledgeStatus status, final String source, final String title, final String tag) {
        Specification<KnowledgeDocument> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(KnowledgeDocumentSpecifications.withStatus(status));
        }
        if (source != null) {
            spec = spec.and(KnowledgeDocumentSpecifications.withSource(source));
        }
        if (title != null) {
            spec = spec.and(KnowledgeDocumentSpecifications.withTitleLike(title));
        }
        if (tag != null) {
            spec = spec.and(KnowledgeDocumentSpecifications.withTag(tag));
        }

        return documentRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public KnowledgeResponse getDocument(final UUID id) {
        final KnowledgeDocument document = documentRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Knowledge document not found"));
        return mapToResponse(document);
    }

    private KnowledgeResponse mapToResponse(final KnowledgeDocument document) {
        final Optional<KnowledgeEmbedding> embeddingOpt = embeddingRepository.findById(document.getId());
        final String metadata =
                embeddingOpt.map(KnowledgeEmbedding::getModelVersion).orElse("No embedding cached yet");

        return KnowledgeResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .source(document.getSource())
                .tags(document.getTags())
                .status(document.getStatus())
                .version(document.getVersion())
                .embeddingMetadata(metadata)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
