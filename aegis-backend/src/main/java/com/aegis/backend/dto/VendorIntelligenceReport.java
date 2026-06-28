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
public class VendorIntelligenceReport {
    private List<String> kpis;
    private List<String> riskAudit;
    private List<String> consolidationOpportunities;
    private List<String> recommendations;
    private Double confidence;
    private String markdownSummary;
}
