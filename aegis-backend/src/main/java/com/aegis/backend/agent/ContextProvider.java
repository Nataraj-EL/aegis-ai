package com.aegis.backend.agent;

public interface ContextProvider {
    void populateContext(AgentContext.Builder builder, String sessionId, String username, String message);
}
