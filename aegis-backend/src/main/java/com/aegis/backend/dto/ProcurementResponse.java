package com.aegis.backend.dto;

import com.aegis.backend.entity.ProcurementStatus;
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
public class ProcurementResponse {
    private UUID id;
    private String itemName;
    private Integer quantity;
    private BigDecimal estimatedCost;
    private String justification;
    private ProcurementStatus status;
    private String username;
    private LocalDateTime createdAt;
}
