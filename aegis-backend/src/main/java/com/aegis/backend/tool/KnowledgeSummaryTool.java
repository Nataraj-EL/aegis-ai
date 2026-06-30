package com.aegis.backend.tool;

import com.aegis.backend.ai.EmbeddingService;
import com.aegis.backend.dto.KnowledgeMetricsProjection;
import com.aegis.backend.entity.KnowledgeDocument;
import com.aegis.backend.entity.KnowledgeStatus;
import com.aegis.backend.repository.KnowledgeDocumentRepository;
import com.aegis.backend.repository.KnowledgeDocumentSpecifications;
import com.aegis.backend.service.MetricsService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KnowledgeSummaryTool implements Tool {

    private final KnowledgeDocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final MetricsService metricsService;

    public KnowledgeSummaryTool(
            final KnowledgeDocumentRepository documentRepository,
            final EmbeddingService embeddingService,
            final MetricsService metricsService) {
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
        this.metricsService = metricsService;
    }

    @Override
    public String getId() {
        return "knowledge_summary";
    }

    @Override
    public String getName() {
        return "Knowledge Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Queries corporate knowledge base documents semantically or textually. Accepts optional 'query', 'limit', and 'minSimilarity' arguments.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        final List<KnowledgeMetricsProjection> metrics = documentRepository.getKnowledgeMetricsSummary();

        long totalCount = 0;
        final Map<String, Long> statusCounts = new HashMap<>();
        for (final KnowledgeStatus status : KnowledgeStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }
        for (final KnowledgeMetricsProjection m : metrics) {
            final KnowledgeStatus s = m.getStatus();
            final long count = m.getCount() != null ? m.getCount() : 0L;
            if (s != null) {
                statusCounts.put(s.name(), count);
            }
            totalCount += count;
        }

        List<KnowledgeDocument> documents = new ArrayList<>();
        String query = null;
        int limit = 5;
        double minSimilarity = 0.6;

        if (arguments != null) {
            if (arguments.get("query") != null) {
                query = (String) arguments.get("query");
            }
            if (arguments.get("limit") != null) {
                try {
                    limit = Integer.parseInt(arguments.get("limit").toString());
                } catch (final Exception exception) {
                    log.warn("Invalid limit argument: {}", arguments.get("limit"), exception);
                }
            }
            if (arguments.get("minSimilarity") != null) {
                try {
                    minSimilarity =
                            Double.parseDouble(arguments.get("minSimilarity").toString());
                } catch (final Exception exception) {
                    log.warn("Invalid minSimilarity argument: {}", arguments.get("minSimilarity"), exception);
                }
            }
        }

        if (query != null && !query.trim().isEmpty()) {
            boolean vectorSuccess = false;
            try {
                log.info("Generating query vector for semantic query: {}", query);
                final float[] vector = embeddingService.generateEmbedding(query);
                final String vectorString = toVectorString(vector);

                log.info("Running pgvector similarity search (threshold: {}, limit: {})...", minSimilarity, limit);
                documents = documentRepository.findSimilarPublished(vectorString, minSimilarity, limit);
                vectorSuccess = true;
                metricsService.incrementRagSearch("semantic_success");
            } catch (final Exception exception) {
                log.warn("Vector similarity search failed. Initiating keyword text fallback.", exception);
            }

            if (!vectorSuccess) {
                // Graceful Fallback: Keyword text lookup using JpaSpecification
                log.info("Running JpaSpecification keyword lookup for query: {}", query);
                final Specification<KnowledgeDocument> textSpec = Specification.where(
                                KnowledgeDocumentSpecifications.withStatus(KnowledgeStatus.PUBLISHED))
                        .and(Specification.where(KnowledgeDocumentSpecifications.withTitleLike(query))
                                .or(KnowledgeDocumentSpecifications.withTag(query)));

                try {
                    documents = documentRepository.findAll(textSpec);
                    if (documents.size() > limit) {
                        documents = documents.subList(0, limit);
                    }
                    metricsService.incrementRagSearch("text_fallback");
                } catch (final Exception exception) {
                    log.error("Keyword text search fallback failed.", exception);
                    metricsService.incrementRagSearch("failure");
                }
            }
        }

        final List<Map<String, Object>> list = new ArrayList<>();
        for (final KnowledgeDocument doc : documents) {
            final Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId().toString());
            map.put("title", doc.getTitle());
            map.put("content", doc.getContent());
            map.put("source", doc.getSource());
            map.put("tags", doc.getTags());
            map.put("status", doc.getStatus().name());
            list.add(map);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("statusCounts", statusCounts);
        result.put("documents", list);
        return result;
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
