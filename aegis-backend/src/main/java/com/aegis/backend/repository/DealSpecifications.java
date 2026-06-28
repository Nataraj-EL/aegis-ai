package com.aegis.backend.repository;

import com.aegis.backend.entity.Deal;
import com.aegis.backend.entity.DealStatus;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class DealSpecifications {

    private DealSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<Deal> withStatus(final DealStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Deal> withCustomerId(final UUID customerId) {
        return (root, query, criteriaBuilder) -> customerId == null
                ? null
                : criteriaBuilder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Deal> amountGreaterThanOrEqualTo(final BigDecimal minAmount) {
        return (root, query, criteriaBuilder) ->
                minAmount == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }
}
