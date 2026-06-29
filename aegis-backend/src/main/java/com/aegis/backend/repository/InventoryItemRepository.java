package com.aegis.backend.repository;

import com.aegis.backend.dto.InventorySummaryProjection;
import com.aegis.backend.entity.InventoryItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository
        extends JpaRepository<InventoryItem, UUID>, JpaSpecificationExecutor<InventoryItem> {
    Optional<InventoryItem> findBySku(String sku);

    @Query(
            "SELECT COUNT(i) AS count, COALESCE(SUM(i.quantity * i.unitPrice), 0) AS totalValuation FROM InventoryItem i")
    InventorySummaryProjection getInventorySummary();
}
