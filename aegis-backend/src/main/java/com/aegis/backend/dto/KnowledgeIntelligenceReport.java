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
public class KnowledgeIntelligenceReport {
    private String query;
    private List<String> retrievedDocumentTitles;
    private String answer;
    private List<String> citations;
    private Double confidence;
    private String markdownSummary;
}
