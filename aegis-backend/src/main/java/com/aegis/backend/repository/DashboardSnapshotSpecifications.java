package com.aegis.backend.repository;

import com.aegis.backend.entity.DashboardSnapshot;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class DashboardSnapshotSpecifications {

    private DashboardSnapshotSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<DashboardSnapshot> withTitleLike(final String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<DashboardSnapshot> createdAfter(final LocalDateTime after) {
        return (root, query, criteriaBuilder) ->
                after == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), after);
    }

    public static Specification<DashboardSnapshot> createdBefore(final LocalDateTime before) {
        return (root, query, criteriaBuilder) ->
                before == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), before);
    }
}
