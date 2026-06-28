package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ChatMessageDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CeoAgent implements Agent {

    private final AiService aiService;
    private final PromptManager promptManager;

    public CeoAgent(final AiService aiService, final PromptManager promptManager) {
        this.aiService = aiService;
        this.promptManager = promptManager;
    }

    @Override
    public String getId() {
        return "ceo";
    }

    @Override
    public String getName() {
        return "CEO Orchestrator";
    }

    @Override
    public String getDescription() {
        return "The master mind agent designed to coordinate, analyze, and draft structured operational strategies.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
                "workflow planning", "task delegation", "document drafting", "general operational query handling");
    }

    @Override
    public AgentChatResponse process(final AgentChatRequest request, final AgentContext context) {
        final String systemPrompt = promptManager.loadSystemPrompt("ceo_agent.txt");

        final List<ChatMessageDto> history = context.getConversationHistory();
        final StringBuilder historyText = new StringBuilder();

        // Retrieve everything except the last element (which is the current user request)
        if (history.size() > 1) {
            for (int i = 0; i < history.size() - 1; i++) {
                final ChatMessageDto msg = history.get(i);
                historyText
                        .append("[")
                        .append(msg.getRole().toUpperCase())
                        .append("]: ")
                        .append(msg.getContent())
                        .append("\n");
            }
        }

        final List<String> retrievedContext = context.getRetrievedContext();
        final StringBuilder contextText = new StringBuilder();
        if (retrievedContext != null && !retrievedContext.isEmpty()) {
            contextText.append("Retrieved Reference Documents:\n");
            for (final String chunk : retrievedContext) {
                contextText.append("- ").append(chunk).append("\n");
            }
            contextText.append("\n");
        }

        final StringBuilder queryBuilder = new StringBuilder();
        if (contextText.length() > 0) {
            queryBuilder.append(contextText);
        }
        if (historyText.length() > 0) {
            queryBuilder.append("Conversation History:\n").append(historyText).append("\n");
        }
        queryBuilder.append("Current User Request:\n[USER]: ").append(request.getMessage());

        final String finalQuery = queryBuilder.toString();

        final long startTime = System.currentTimeMillis();
        final String responseText = aiService.generateResponse(systemPrompt, finalQuery);
        final long duration = System.currentTimeMillis() - startTime;

        return AgentChatResponse.builder()
                .response(responseText)
                .executionTime(duration)
                .model("gemini-1.5-flash")
                .build();
    }
}
