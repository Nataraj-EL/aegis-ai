package com.aegis.backend.dto;

import com.aegis.backend.entity.DealStatus;
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
public class DealResponse {
    private UUID id;
    private String title;
    private BigDecimal amount;
    private DealStatus status;
    private UUID customerId;
    private String customerName;
    private String username;
    private LocalDateTime createdAt;
}
