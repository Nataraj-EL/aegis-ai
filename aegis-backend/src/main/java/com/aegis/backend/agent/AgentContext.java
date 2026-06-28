package com.aegis.backend.agent;

import com.aegis.backend.dto.ChatMessageDto;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class AgentContext {

    private final String sessionId;
    private final String username;
    private final String requestId;
    private final List<ChatMessageDto> conversationHistory;
    private final List<String> retrievedContext;
    private final BiConsumer<String, String> saveMessageCallback;

    private AgentContext(final Builder builder) {
        this.sessionId = builder.sessionId;
        this.username = builder.username;
        this.requestId = builder.requestId;
        this.conversationHistory = builder.conversationHistory;
        this.retrievedContext = builder.retrievedContext;
        this.saveMessageCallback = builder.saveMessageCallback;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public String getRequestId() {
        return requestId;
    }

    public List<ChatMessageDto> getConversationHistory() {
        return conversationHistory;
    }

    public List<String> getRetrievedContext() {
        return retrievedContext;
    }

    public void saveMessage(final String role, final String content) {
        if (saveMessageCallback != null) {
            saveMessageCallback.accept(role, content);
        }
    }

    public String getFormattedHistory() {
        final StringBuilder historyText = new StringBuilder();
        if (conversationHistory != null && conversationHistory.size() > 1) {
            for (int i = 0; i < conversationHistory.size() - 1; i++) {
                final ChatMessageDto msg = conversationHistory.get(i);
                historyText
                        .append("[")
                        .append(msg.getRole().toUpperCase())
                        .append("]: ")
                        .append(msg.getContent())
                        .append("\n");
            }
        }
        return historyText.toString();
    }

    public String getFormattedRetrievedContext() {
        final StringBuilder contextText = new StringBuilder();
        if (retrievedContext != null && !retrievedContext.isEmpty()) {
            contextText.append("Retrieved Reference Documents:\n");
            for (final String chunk : retrievedContext) {
                contextText.append("- ").append(chunk).append("\n");
            }
            contextText.append("\n");
        }
        return contextText.toString();
    }

    public String buildQuery(final String metricsContext, final String userMessage) {
        final StringBuilder queryBuilder = new StringBuilder();
        if (metricsContext != null && !metricsContext.isEmpty()) {
            queryBuilder.append(metricsContext).append("\n");
        }

        final String contextText = getFormattedRetrievedContext();
        if (!contextText.isEmpty()) {
            queryBuilder.append(contextText);
        }

        final String historyText = getFormattedHistory();
        if (!historyText.isEmpty()) {
            queryBuilder.append("Conversation History:\n").append(historyText).append("\n");
        }

        queryBuilder.append("Current User Request:\n[USER]: ").append(userMessage);
        return queryBuilder.toString();
    }

    public static class Builder {
        private final String sessionId;
        private final String username;
        private String requestId;
        private List<ChatMessageDto> conversationHistory = new ArrayList<>();
        private List<String> retrievedContext = new ArrayList<>();
        private BiConsumer<String, String> saveMessageCallback;

        public Builder(final String sessionId, final String username) {
            this.sessionId = sessionId;
            this.username = username;
        }

        public Builder requestId(final String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder conversationHistory(final List<ChatMessageDto> conversationHistory) {
            this.conversationHistory = conversationHistory;
            return this;
        }

        public Builder retrievedContext(final List<String> retrievedContext) {
            this.retrievedContext = retrievedContext;
            return this;
        }

        public Builder saveMessageCallback(final BiConsumer<String, String> saveMessageCallback) {
            this.saveMessageCallback = saveMessageCallback;
            return this;
        }

        public AgentContext build() {
            return new AgentContext(this);
        }
    }
}
