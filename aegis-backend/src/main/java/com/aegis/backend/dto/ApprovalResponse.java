package com.aegis.backend.dto;

import com.aegis.backend.entity.ApprovalStatus;
import com.aegis.backend.entity.ApprovalType;
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
public class ApprovalResponse {
    private UUID id;
    private ApprovalType entityType;
    private UUID entityId;
    private String requester;
    private String approver;
    private ApprovalStatus status;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
