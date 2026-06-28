package com.aegis.backend.repository;

import com.aegis.backend.entity.Expense;
import com.aegis.backend.entity.ExpenseCategory;
import com.aegis.backend.entity.ExpenseStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<Expense> withUsername(final String username) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), username);
    }

    public static Specification<Expense> withCategory(final ExpenseCategory category) {
        return (root, query, criteriaBuilder) ->
                category == null ? null : criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Expense> withStatus(final ExpenseStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Expense> createdAfter(final LocalDateTime fromDate) {
        return (root, query, criteriaBuilder) ->
                fromDate == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<Expense> createdBefore(final LocalDateTime toDate) {
        return (root, query, criteriaBuilder) ->
                toDate == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
