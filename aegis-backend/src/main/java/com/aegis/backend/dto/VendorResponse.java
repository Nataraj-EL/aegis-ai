package com.aegis.backend.dto;

import com.aegis.backend.entity.VendorStatus;
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
public class VendorResponse {
    private UUID id;
    private String name;
    private String contactEmail;
    private String category;
    private VendorStatus status;
    private BigDecimal rating;
    private LocalDateTime createdAt;
}
