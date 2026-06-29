package com.aegis.backend.repository;

import com.aegis.backend.dto.KnowledgeMetricsProjection;
import com.aegis.backend.entity.KnowledgeDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeDocumentRepository
        extends JpaRepository<KnowledgeDocument, UUID>, JpaSpecificationExecutor<KnowledgeDocument> {

    @Query(
            value = "SELECT kd.* FROM knowledge_documents kd "
                    + "JOIN knowledge_embeddings ke ON kd.id = ke.document_id "
                    + "WHERE kd.status = 'PUBLISHED' "
                    + "AND (1 - (ke.embedding <=> CAST(:embedding AS vector))) >= :minSimilarity "
                    + "ORDER BY ke.embedding <=> CAST(:embedding AS vector) "
                    + "LIMIT :limit",
            nativeQuery = true)
    List<KnowledgeDocument> findSimilarPublished(
            @Param("embedding") String embedding,
            @Param("minSimilarity") double minSimilarity,
            @Param("limit") int limit);

    @Query("SELECT k.status AS status, COUNT(k) AS count FROM KnowledgeDocument k GROUP BY k.status")
    List<KnowledgeMetricsProjection> getKnowledgeMetricsSummary();
}
