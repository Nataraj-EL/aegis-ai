package com.aegis.backend.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveDashboardReport {
    private String query;
    private Map<String, Object> summaryDataParsed;
    private String financialHealth;
    private List<String> operationalBottlenecks;
    private List<String> strategicSuggestions;
    private Double confidence;
    private String markdownSummary;
}
