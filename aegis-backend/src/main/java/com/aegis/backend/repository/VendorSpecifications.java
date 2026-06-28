package com.aegis.backend.repository;

import com.aegis.backend.entity.Vendor;
import com.aegis.backend.entity.VendorStatus;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class VendorSpecifications {

    private VendorSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<Vendor> withStatus(final VendorStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Vendor> withCategory(final String category) {
        return (root, query, criteriaBuilder) ->
                category == null || category.trim().isEmpty()
                        ? null
                        : criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Vendor> ratingGreaterThanOrEqualTo(final BigDecimal minRating) {
        return (root, query, criteriaBuilder) ->
                minRating == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
    }
}
