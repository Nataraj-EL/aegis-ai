package com.aegis.backend.dto;

import com.aegis.backend.entity.ApprovalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalCreateRequest {

    @NotNull(message = "Entity type is required")
    private ApprovalType entityType;

    @NotNull(message = "Entity ID is required")
    private UUID entityId;

    @NotBlank(message = "Approver is required")
    private String approver;
}
