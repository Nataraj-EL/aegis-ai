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
public class CustomerIntelligenceReport {
    private List<String> kpis;
    private List<String> retentionRisks;
    private List<String> pipelineOpportunities;
    private List<String> expansionStrategies;
    private Double confidence;
    private String markdownSummary;
}
