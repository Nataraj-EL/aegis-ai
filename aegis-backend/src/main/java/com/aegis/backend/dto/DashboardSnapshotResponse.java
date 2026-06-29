package com.aegis.backend.dto;

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
public class DashboardSnapshotResponse {
    private UUID id;
    private String title;
    private DashboardSummaryResponse summary;
    private LocalDateTime createdAt;
}
