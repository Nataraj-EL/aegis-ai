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
public class AgentChatResponse {
    private String response;
    private String reasoningSummary;
    private Double confidence;
    private List<String> suggestedActions;
    private Long executionTime;
    private String model;
}
