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
public class ExecutiveInsightsReport {
    private List<String> kpis;
    private List<String> trends;
    private List<String> risks;
    private List<String> recommendations;
    private Double confidence;
    private String markdownSummary;
}
