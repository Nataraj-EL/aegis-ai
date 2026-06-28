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
public class ExpenseIntelligenceReport {
    private List<String> kpis;
    private List<String> anomalies;
    private List<String> budgetRisks;
    private List<String> savingsOpportunities;
    private Double confidence;
    private String markdownSummary;
}
