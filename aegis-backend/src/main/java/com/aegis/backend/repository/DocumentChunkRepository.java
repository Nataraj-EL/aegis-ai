package com.aegis.backend.repository;

import com.aegis.backend.entity.DocumentChunk;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    @Query(
            value =
                    "SELECT content FROM document_chunks ORDER BY embedding <=> CAST(:embedding AS vector) LIMIT :limit",
            nativeQuery = true)
    List<String> findSimilarContent(@Param("embedding") String embedding, @Param("limit") int limit);
}
