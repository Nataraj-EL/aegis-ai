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
public class SalesIntelligenceReport {
    private List<String> kpis;
    private List<String> pipelineRisks;
    private List<String> salesOpportunities;
    private List<String> strategyOptimizations;
    private Double confidence;
    private String markdownSummary;
}
