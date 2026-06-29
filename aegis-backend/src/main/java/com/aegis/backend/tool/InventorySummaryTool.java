package com.aegis.backend.tool;

import com.aegis.backend.entity.InventoryItem;
import com.aegis.backend.entity.InventoryStatus;
import com.aegis.backend.repository.InventoryItemRepository;
import com.aegis.backend.repository.InventoryItemSpecifications;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventorySummaryTool implements Tool {

    private final InventoryItemRepository inventoryItemRepository;

    public InventorySummaryTool(final InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    public String getId() {
        return "inventory_summary";
    }

    @Override
    public String getName() {
        return "Inventory Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates corporate inventory telemetry. Accepts optional 'status' and 'sku' filters.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        Specification<InventoryItem> spec = Specification.where(null);

        if (arguments != null) {
            if (arguments.get("status") != null) {
                final String statusStr = (String) arguments.get("status");
                try {
                    spec = spec.and(
                            InventoryItemSpecifications.withStatus(InventoryStatus.valueOf(statusStr.toUpperCase())));
                } catch (final Exception exception) {
                    log.warn("Invalid stock status supplied to summary tool: {}", statusStr, exception);
                }
            }
            if (arguments.get("sku") != null) {
                spec = spec.and(InventoryItemSpecifications.skuLike((String) arguments.get("sku")));
            }
        }

        final List<InventoryItem> items = inventoryItemRepository.findAll(spec);

        int totalCount = 0;
        int inStockCount = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;
        BigDecimal assetValuationSum = BigDecimal.ZERO;

        final List<Map<String, Object>> list = new ArrayList<>();
        for (final InventoryItem item : items) {
            totalCount++;
            if (item.getStatus() == InventoryStatus.IN_STOCK) {
                inStockCount++;
            } else if (item.getStatus() == InventoryStatus.LOW_STOCK) {
                lowStockCount++;
            } else if (item.getStatus() == InventoryStatus.OUT_OF_STOCK) {
                outOfStockCount++;
            }

            if (item.getQuantity() != null && item.getUnitPrice() != null) {
                final BigDecimal valuation = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                assetValuationSum = assetValuationSum.add(valuation);
            }

            final Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId().toString());
            map.put("sku", item.getSku());
            map.put("name", item.getName());
            map.put("quantity", item.getQuantity() != null ? item.getQuantity() : 0);
            map.put("reorderThreshold", item.getReorderThreshold() != null ? item.getReorderThreshold() : 0);
            map.put("unitPrice", item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);
            map.put("status", item.getStatus().name());
            list.add(map);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("inStockCount", inStockCount);
        result.put("lowStockCount", lowStockCount);
        result.put("outOfStockCount", outOfStockCount);
        result.put("assetValuationSum", assetValuationSum);
        result.put("items", list);
        return result;
    }
}
