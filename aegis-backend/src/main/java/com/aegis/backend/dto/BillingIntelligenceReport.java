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
public class BillingIntelligenceReport {
    private List<String> kpis;
    private List<String> paymentRisks;
    private List<String> cashFlowForecasts;
    private List<String> collectionStrategies;
    private Double confidence;
    private String markdownSummary;
}
