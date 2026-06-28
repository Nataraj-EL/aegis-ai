package com.aegis.backend.util;

import com.aegis.backend.dto.CustomerIntelligenceReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerIntelligenceParser {

    private final ObjectMapper objectMapper;

    public CustomerIntelligenceParser(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CustomerIntelligenceReport parse(final String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException("Raw text to parse cannot be empty");
        }

        final String cleaned = JsonCleaner.cleanMarkdownWrapper(rawText);

        try {
            return objectMapper.readValue(cleaned, CustomerIntelligenceReport.class);
        } catch (final Exception exception) {
            log.error(
                    "Failed to parse raw text as CustomerIntelligenceReport: {}",
                    cleaned.substring(0, Math.min(cleaned.length(), 100)),
                    exception);
            throw new IllegalArgumentException("Failed to parse JSON content: " + exception.getMessage(), exception);
        }
    }
}
