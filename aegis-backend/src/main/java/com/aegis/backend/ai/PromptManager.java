package com.aegis.backend.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PromptManager {

    public PromptManager() {
        // Default constructor
    }

    public String loadSystemPrompt(final String fileName) {
        return loadPrompt("prompts/system/" + fileName);
    }

    public String loadTemplatePrompt(final String fileName) {
        return loadPrompt("prompts/templates/" + fileName);
    }

    private String loadPrompt(final String path) {
        try {
            final ClassPathResource resource = new ClassPathResource(path);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            log.error("Failed to load prompt from path: {}", path, exception);
            throw new IllegalStateException("Failed to load prompt: " + path, exception);
        }
    }
}
