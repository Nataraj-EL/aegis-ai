package com.aegis.backend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalIntelligenceReport {
    private List<String> kpis;
    private List<String> pendingSummary;
    private List<String> risks;
    private List<String> priorityOrder;
    private Double confidence;
    private String markdownSummary;
}
