package com.aegis.backend.dto;

import com.aegis.backend.entity.CustomerStatus;
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
public class CustomerResponse {
    private UUID id;
    private String name;
    private String contactEmail;
    private String industry;
    private CustomerStatus status;
    private BigDecimal totalRevenue;
    private LocalDateTime createdAt;
}
