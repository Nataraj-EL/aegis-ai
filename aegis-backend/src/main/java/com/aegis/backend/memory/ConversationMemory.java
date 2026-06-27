package com.aegis.backend.memory;

import com.aegis.backend.dto.ChatMessageDto;
import java.util.List;

public interface ConversationMemory extends Memory {
    void addMessage(String sessionId, String role, String content);

    List<ChatMessageDto> getMessages(String sessionId);
}
