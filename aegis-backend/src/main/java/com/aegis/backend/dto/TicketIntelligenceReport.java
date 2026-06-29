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
public class TicketIntelligenceReport {
    private List<String> kpis;
    private List<String> slaEscalationRisks;
    private List<String> resolutionForecasts;
    private List<String> priorityStrategies;
    private Double confidence;
    private String markdownSummary;
}
