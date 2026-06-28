package com.aegis.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementCreateRequest {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Estimated cost is required")
    @Positive(message = "Estimated cost must be positive")
    private BigDecimal estimatedCost;

    @NotBlank(message = "Justification is required")
    private String justification;

    @NotBlank(message = "Approver is required")
    private String approver;
}
