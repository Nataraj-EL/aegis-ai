package com.aegis.backend.repository;

import com.aegis.backend.entity.ProcurementRequest;
import com.aegis.backend.entity.ProcurementStatus;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ProcurementSpecifications {

    private ProcurementSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<ProcurementRequest> withUsername(final String username) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), username);
    }

    public static Specification<ProcurementRequest> withStatus(final ProcurementStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<ProcurementRequest> costGreaterThanOrEqualTo(final BigDecimal minCost) {
        return (root, query, criteriaBuilder) ->
                minCost == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("estimatedCost"), minCost);
    }

    public static Specification<ProcurementRequest> costLessThanOrEqualTo(final BigDecimal maxCost) {
        return (root, query, criteriaBuilder) ->
                maxCost == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("estimatedCost"), maxCost);
    }
}
