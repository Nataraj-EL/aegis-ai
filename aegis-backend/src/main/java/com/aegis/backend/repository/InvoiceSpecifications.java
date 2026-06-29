package com.aegis.backend.repository;

import com.aegis.backend.entity.Invoice;
import com.aegis.backend.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class InvoiceSpecifications {

    private InvoiceSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<Invoice> withStatus(final InvoiceStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Invoice> withCustomerId(final UUID customerId) {
        return (root, query, criteriaBuilder) -> customerId == null
                ? null
                : criteriaBuilder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Invoice> dueDateBetween(final LocalDateTime from, final LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from != null && to != null) {
                return criteriaBuilder.between(root.get("dueDate"), from, to);
            }
            if (from != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), from);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), to);
        };
    }

    public static Specification<Invoice> amountGreaterThanOrEqualTo(final BigDecimal minAmount) {
        return (root, query, criteriaBuilder) ->
                minAmount == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }
}
