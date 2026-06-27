package com.aegis.backend.memory;

import com.aegis.backend.dto.ChatMessageDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MemoryManager {

    private final ConversationMemory conversationMemory;

    public MemoryManager(final ConversationMemory conversationMemory) {
        this.conversationMemory = conversationMemory;
    }

    public void saveMessage(final String sessionId, final String role, final String content) {
        conversationMemory.addMessage(sessionId, role, content);
    }

    public List<ChatMessageDto> getConversationHistory(final String sessionId) {
        return conversationMemory.getMessages(sessionId);
    }

    public void clearSession(final String sessionId) {
        conversationMemory.clear(sessionId);
    }
}
