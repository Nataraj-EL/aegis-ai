package com.aegis.backend.dto;

import com.aegis.backend.entity.VendorStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorCreateRequest {

    @NotBlank(message = "Vendor name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Status is required")
    private VendorStatus status;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    private BigDecimal rating;
}
