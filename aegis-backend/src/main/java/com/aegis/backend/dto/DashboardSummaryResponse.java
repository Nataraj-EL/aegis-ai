package com.aegis.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private Long expenseCount;
    private BigDecimal expenseTotalAmount;
    private Long procurementCount;
    private BigDecimal procurementTotalCost;
    private BigDecimal procurementPendingCost;
    private Long vendorCount;
    private Double averageVendorRating;
    private Long customerCount;
    private Long dealCount;
    private BigDecimal dealTotalValue;
    private BigDecimal dealClosedWonValue;
    private Long inventoryItemCount;
    private BigDecimal inventoryTotalValuation;
    private Long invoiceCount;
    private BigDecimal invoiceTotalAmount;
    private BigDecimal invoicePaidAmount;
    private BigDecimal invoiceOutstandingAmount;
    private Long ticketCount;
    private Long knowledgeDocCount;
    private LocalDateTime createdAt;
}
