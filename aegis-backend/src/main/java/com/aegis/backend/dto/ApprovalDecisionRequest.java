package com.aegis.backend.dto;

import com.aegis.backend.entity.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDecisionRequest {

    @NotNull(message = "Decision status is required")
    private ApprovalStatus status;

    private String comments;
}
