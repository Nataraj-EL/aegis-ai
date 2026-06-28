package com.aegis.backend.dto;

import com.aegis.backend.entity.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {

    @NotBlank(message = "Customer name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotNull(message = "Status is required")
    private CustomerStatus status;

    @NotNull(message = "Total revenue is required")
    @PositiveOrZero(message = "Total revenue must be positive or zero")
    private BigDecimal totalRevenue;
}
