package com.aegis.backend.repository;

import com.aegis.backend.entity.Ticket;
import com.aegis.backend.entity.TicketPriority;
import com.aegis.backend.entity.TicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecifications {

    private TicketSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<Ticket> withStatus(final TicketStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Ticket> withPriority(final TicketPriority priority) {
        return (root, query, criteriaBuilder) ->
                priority == null ? null : criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> withCustomerId(final UUID customerId) {
        return (root, query, criteriaBuilder) -> customerId == null
                ? null
                : criteriaBuilder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Ticket> withAssignee(final String assignee) {
        return (root, query, criteriaBuilder) -> {
            if (assignee == null || assignee.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("assignee")), "%" + assignee.toLowerCase() + "%");
        };
    }

    public static Specification<Ticket> createdAtBetween(final LocalDateTime from, final LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from != null && to != null) {
                return criteriaBuilder.between(root.get("createdAt"), from, to);
            }
            if (from != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}
