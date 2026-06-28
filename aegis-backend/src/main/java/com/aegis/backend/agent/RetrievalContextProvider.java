package com.aegis.backend.agent;

import com.aegis.backend.rag.RetrievalService;
import org.springframework.stereotype.Component;

@Component
public class RetrievalContextProvider implements ContextProvider {

    private final RetrievalService retrievalService;

    public RetrievalContextProvider(final RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public void populateContext(
            final AgentContext.Builder builder, final String sessionId, final String username, final String message) {
        builder.retrievedContext(retrievalService.retrieveContext(message, 3));
    }
}
