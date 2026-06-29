package com.aegis.backend.repository;

import com.aegis.backend.entity.KnowledgeEmbedding;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeEmbeddingRepository extends JpaRepository<KnowledgeEmbedding, UUID> {}
