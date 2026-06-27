package com.aegis.backend.ai;

public interface AiService {
    String generateResponse(String systemPrompt, String userMessage);
}
