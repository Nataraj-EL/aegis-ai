package com.aegis.backend.repository;

import com.aegis.backend.entity.Customer;
import com.aegis.backend.entity.CustomerStatus;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecifications {

    private CustomerSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<Customer> withStatus(final CustomerStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Customer> withIndustry(final String industry) {
        return (root, query, criteriaBuilder) ->
                industry == null || industry.trim().isEmpty()
                        ? null
                        : criteriaBuilder.equal(root.get("industry"), industry);
    }

    public static Specification<Customer> revenueGreaterThanOrEqualTo(final BigDecimal minRevenue) {
        return (root, query, criteriaBuilder) ->
                minRevenue == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("totalRevenue"), minRevenue);
    }
}
