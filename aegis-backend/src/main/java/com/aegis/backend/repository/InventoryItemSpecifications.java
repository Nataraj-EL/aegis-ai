package com.aegis.backend.repository;

import com.aegis.backend.entity.InventoryItem;
import com.aegis.backend.entity.InventoryStatus;
import org.springframework.data.jpa.domain.Specification;

public final class InventoryItemSpecifications {

    private InventoryItemSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<InventoryItem> withStatus(final InventoryStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<InventoryItem> skuLike(final String sku) {
        return (root, query, criteriaBuilder) -> sku == null || sku.trim().isEmpty()
                ? null
                : criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), "%" + sku.toLowerCase() + "%");
    }

    public static Specification<InventoryItem> quantityLessThanOrEqualTo(final Integer maxQuantity) {
        return (root, query, criteriaBuilder) ->
                maxQuantity == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), maxQuantity);
    }
}
