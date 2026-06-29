package com.aegis.backend.dto;

import com.aegis.backend.entity.InvoiceStatus;
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
public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private BigDecimal amount;
    private InvoiceStatus status;
    private UUID customerId;
    private String customerName;
    private UUID dealId;
    private String dealTitle;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}
