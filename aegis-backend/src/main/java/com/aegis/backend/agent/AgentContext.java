package com.aegis.backend.agent;

import com.aegis.backend.dto.ChatMessageDto;
import com.aegis.backend.memory.MemoryManager;
import java.util.List;

public class AgentContext {

    private final String sessionId;
    private final String username;
    private final MemoryManager memoryManager;

    public AgentContext(final String sessionId, final String username, final MemoryManager memoryManager) {
        this.sessionId = sessionId;
        this.username = username;
        this.memoryManager = memoryManager;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public List<ChatMessageDto> getConversationHistory() {
        return memoryManager.getConversationHistory(sessionId);
    }

    public void saveMessage(final String role, final String content) {
        memoryManager.saveMessage(sessionId, role, content);
    }
}
