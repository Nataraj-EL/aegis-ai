package com.aegis.backend.agent;

import com.aegis.backend.memory.MemoryManager;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AgentContextFactory {

    private final List<ContextProvider> contextProviders;
    private final MemoryManager memoryManager;

    public AgentContextFactory(final List<ContextProvider> contextProviders, final MemoryManager memoryManager) {
        this.contextProviders = contextProviders;
        this.memoryManager = memoryManager;
    }

    public AgentContext createContext(final String sessionId, final String username, final String message) {
        final AgentContext.Builder builder = new AgentContext.Builder(sessionId, username);

        for (final ContextProvider provider : contextProviders) {
            provider.populateContext(builder, sessionId, username, message);
        }

        builder.saveMessageCallback((role, content) -> memoryManager.saveMessage(sessionId, role, content));

        return builder.build();
    }
}
