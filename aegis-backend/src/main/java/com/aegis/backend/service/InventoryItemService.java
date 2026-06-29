package com.aegis.backend.service;

import com.aegis.backend.dto.InventoryCreateRequest;
import com.aegis.backend.dto.InventoryResponse;
import com.aegis.backend.entity.InventoryItem;
import com.aegis.backend.entity.InventoryStatus;
import com.aegis.backend.event.StockLevelAlertEvent;
import com.aegis.backend.repository.InventoryItemRepository;
import com.aegis.backend.repository.InventoryItemSpecifications;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryItemService(
            final InventoryItemRepository inventoryItemRepository, final ApplicationEventPublisher eventPublisher) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public InventoryResponse createInventoryItem(final InventoryCreateRequest request) {
        if (inventoryItemRepository.findBySku(request.getSku()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Inventory item with SKU '%s' already exists", request.getSku()));
        }

        final InventoryItem item = InventoryItem.builder()
                .sku(request.getSku())
                .name(request.getName())
                .quantity(request.getQuantity())
                .reorderThreshold(request.getReorderThreshold())
                .unitPrice(request.getUnitPrice())
                .build();

        item.updateStatus();
        final InventoryItem saved = inventoryItemRepository.save(item);

        if (saved.getStatus() == InventoryStatus.LOW_STOCK || saved.getStatus() == InventoryStatus.OUT_OF_STOCK) {
            eventPublisher.publishEvent(new StockLevelAlertEvent(
                    this, saved.getId(), saved.getSku(), null, saved.getStatus(), saved.getQuantity()));
        }

        return mapToResponse(saved);
    }

    @Transactional
    public InventoryResponse updateQuantity(final UUID id, final Integer quantityChange) {
        final InventoryItem item = inventoryItemRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));

        final int newQuantity = item.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Inventory quantity cannot be negative");
        }

        final InventoryStatus oldStatus = item.getStatus();
        item.setQuantity(newQuantity);
        item.updateStatus();

        final InventoryItem saved = inventoryItemRepository.save(item);
        final InventoryStatus newStatus = saved.getStatus();

        if (newStatus != oldStatus
                && (newStatus == InventoryStatus.LOW_STOCK || newStatus == InventoryStatus.OUT_OF_STOCK)) {
            eventPublisher.publishEvent(new StockLevelAlertEvent(
                    this, saved.getId(), saved.getSku(), oldStatus, newStatus, saved.getQuantity()));
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryItems(
            final InventoryStatus status, final String sku, final Integer maxQuantity) {
        Specification<InventoryItem> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(InventoryItemSpecifications.withStatus(status));
        }
        if (sku != null) {
            spec = spec.and(InventoryItemSpecifications.skuLike(sku));
        }
        if (maxQuantity != null) {
            spec = spec.and(InventoryItemSpecifications.quantityLessThanOrEqualTo(maxQuantity));
        }

        return inventoryItemRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryItem(final UUID id) {
        final InventoryItem item = inventoryItemRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
        return mapToResponse(item);
    }

    private InventoryResponse mapToResponse(final InventoryItem item) {
        return InventoryResponse.builder()
                .id(item.getId())
                .sku(item.getSku())
                .name(item.getName())
                .quantity(item.getQuantity())
                .reorderThreshold(item.getReorderThreshold())
                .unitPrice(item.getUnitPrice())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
