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
public class InventoryIntelligenceReport {
    private List<String> kpis;
    private List<String> stockoutRisks;
    private List<String> valuationInsights;
    private List<String> reorderRecommendations;
    private Double confidence;
    private String markdownSummary;
}
