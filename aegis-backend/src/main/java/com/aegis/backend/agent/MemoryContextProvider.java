package com.aegis.backend.agent;

import com.aegis.backend.memory.MemoryManager;
import org.springframework.stereotype.Component;

@Component
public class MemoryContextProvider implements ContextProvider {

    private final MemoryManager memoryManager;

    public MemoryContextProvider(final MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    @Override
    public void populateContext(
            final AgentContext.Builder builder, final String sessionId, final String username, final String message) {
        builder.conversationHistory(memoryManager.getConversationHistory(sessionId));
    }
}
