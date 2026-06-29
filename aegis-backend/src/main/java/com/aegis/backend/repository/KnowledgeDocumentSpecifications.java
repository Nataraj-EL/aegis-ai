package com.aegis.backend.repository;

import com.aegis.backend.entity.KnowledgeDocument;
import com.aegis.backend.entity.KnowledgeStatus;
import org.springframework.data.jpa.domain.Specification;

public final class KnowledgeDocumentSpecifications {

    private KnowledgeDocumentSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<KnowledgeDocument> withStatus(final KnowledgeStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<KnowledgeDocument> withSource(final String source) {
        return (root, query, criteriaBuilder) -> {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("source"), source);
        };
    }

    public static Specification<KnowledgeDocument> withTitleLike(final String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<KnowledgeDocument> withTag(final String tag) {
        return (root, query, criteriaBuilder) -> {
            if (tag == null || tag.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("tags")), "%" + tag.toLowerCase() + "%");
        };
    }
}
