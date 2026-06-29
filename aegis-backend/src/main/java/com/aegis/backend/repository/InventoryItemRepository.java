package com.aegis.backend.repository;

import com.aegis.backend.entity.InventoryItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository
        extends JpaRepository<InventoryItem, UUID>, JpaSpecificationExecutor<InventoryItem> {
    Optional<InventoryItem> findBySku(String sku);
}
