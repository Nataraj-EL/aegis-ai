package com.aegis.backend.util;

public final class JsonCleaner {

    private JsonCleaner() {
        // Private constructor to prevent instantiation
    }

    public static String cleanMarkdownWrapper(final String rawText) {
        if (rawText == null) {
            return "";
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
        return cleaned.trim();
    }
}
