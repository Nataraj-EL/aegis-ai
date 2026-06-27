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

        final String userMessageWithHistory;
        if (historyText.length() > 0) {
            userMessageWithHistory = "Conversation History:\n" + historyText.toString() + "\n"
                    + "Current User Request:\n[USER]: " + request.getMessage();
        } else {
            userMessageWithHistory = request.getMessage();
        }

        final long startTime = System.currentTimeMillis();
        final String responseText = aiService.generateResponse(systemPrompt, userMessageWithHistory);
        final long duration = System.currentTimeMillis() - startTime;

        return AgentChatResponse.builder()
                .response(responseText)
                .executionTime(duration)
                .model("gemini-1.5-flash")
                .build();
    }
}
