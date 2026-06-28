package com.aegis.backend.dto;

import com.aegis.backend.entity.ExpenseCategory;
import com.aegis.backend.entity.ExpenseStatus;
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
public class ExpenseResponse {
    private UUID id;
    private String description;
    private BigDecimal amount;
    private ExpenseCategory category;
    private ExpenseStatus status;
    private String username;
    private LocalDateTime createdAt;
}
