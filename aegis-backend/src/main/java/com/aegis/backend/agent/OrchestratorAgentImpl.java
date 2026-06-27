package com.aegis.backend.agent;

import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.memory.MemoryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrchestratorAgentImpl implements OrchestratorAgent {

    private final AgentRouter agentRouter;
    private final MemoryManager memoryManager;

    public OrchestratorAgentImpl(final AgentRouter agentRouter, final MemoryManager memoryManager) {
        this.agentRouter = agentRouter;
        this.memoryManager = memoryManager;
    }

    @Override
    public AgentChatResponse process(final AgentChatRequest request) {
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Orchestrator creating context for session/user: {}", username);

        final AgentContext context = new AgentContext(username, username, memoryManager);

        // Save incoming user message
        context.saveMessage("user", request.getMessage());

        log.info("Orchestrator routing request message: {}", request.getMessage());
        final Agent routedAgent = agentRouter.route(request.getMessage());

        if (routedAgent == null) {
            log.error("No suitable executing agent resolved for request.");
            return AgentChatResponse.builder()
                    .response("Error: No suitable executing agent could be resolved to handle this request.")
                    .build();
        }

        log.info("Delegating execution process to Agent: {}", routedAgent.getName());
        final AgentChatResponse response = routedAgent.process(request, context);

        // Save final agent response
        if (response != null && response.getResponse() != null) {
            context.saveMessage("agent", response.getResponse());
        }

        return response;
    }
}
