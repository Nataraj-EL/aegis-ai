package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import org.springframework.stereotype.Service;

@Service
public class CeoAgent implements OrchestratorAgent {

    private final AiService aiService;
    private final PromptManager promptManager;

    public CeoAgent(final AiService aiService, final PromptManager promptManager) {
        this.aiService = aiService;
        this.promptManager = promptManager;
    }

    @Override
    public AgentChatResponse process(final AgentChatRequest request) {
        final String systemPrompt = promptManager.loadSystemPrompt("ceo_agent.txt");

        final long startTime = System.currentTimeMillis();
        final String responseText = aiService.generateResponse(systemPrompt, request.getMessage());
        final long duration = System.currentTimeMillis() - startTime;

        return AgentChatResponse.builder()
                .response(responseText)
                .executionTime(duration)
                .model("gemini-1.5-flash")
                .build();
    }
}
