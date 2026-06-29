package com.aegis.backend.dto;

import com.aegis.backend.entity.InventoryStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private UUID id;
    private String sku;
    private String name;
    private Integer quantity;
    private Integer reorderThreshold;
    private BigDecimal unitPrice;
    private InventoryStatus status;
    private LocalDateTime createdAt;
}
