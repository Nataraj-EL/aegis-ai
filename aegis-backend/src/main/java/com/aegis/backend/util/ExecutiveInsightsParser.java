package com.aegis.backend.util;

import com.aegis.backend.dto.ExecutiveInsightsReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecutiveInsightsParser {

    private final ObjectMapper objectMapper;

    public ExecutiveInsightsParser(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ExecutiveInsightsReport parse(final String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("Raw text to parse cannot be empty");
        }

        String cleaned = rawText.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            return objectMapper.readValue(cleaned, ExecutiveInsightsReport.class);
        } catch (final Exception exception) {
            log.error(
                    "Failed to parse raw text as ExecutiveInsightsReport: {}",
                    cleaned.substring(0, Math.min(cleaned.length(), 100)),
                    exception);
            throw new IllegalArgumentException("Failed to parse JSON content: " + exception.getMessage(), exception);
        }
    }
}
