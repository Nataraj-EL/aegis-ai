package com.aegis.backend.agent;

import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.service.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrchestratorAgentImpl implements OrchestratorAgent {

    private final AgentRouter agentRouter;
    private final AgentContextFactory agentContextFactory;
    private final MetricsService metricsService;

    public OrchestratorAgentImpl(
            final AgentRouter agentRouter,
            final AgentContextFactory agentContextFactory,
            final MetricsService metricsService) {
        this.agentRouter = agentRouter;
        this.agentContextFactory = agentContextFactory;
        this.metricsService = metricsService;
    }

    @Override
    public AgentChatResponse process(final AgentChatRequest request) {
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Orchestrator creating context for session/user: {}", username);

        // Build the execution context using the Factory
        final AgentContext context = agentContextFactory.createContext(username, username, request.getMessage());

        // Save incoming user message to memory via AgentContext delegate
        context.saveMessage("user", request.getMessage());

        log.info("Orchestrator routing request message: {}", request.getMessage());
        final Agent routedAgent = agentRouter.route(request.getMessage());

        if (routedAgent == null) {
            log.error("No suitable executing agent resolved for request.");
            metricsService.recordAgentExecution("orchestrator", "failure", 0L);
            return AgentChatResponse.builder()
                    .response("Error: No suitable executing agent could be resolved to handle this request.")
                    .build();
        }

        log.info("Delegating execution process to Agent: {}", routedAgent.getName());
        final long startTime = System.currentTimeMillis();
        try {
            final AgentChatResponse response = routedAgent.process(request, context);
            final long duration = System.currentTimeMillis() - startTime;
            metricsService.recordAgentExecution(routedAgent.getId(), "success", duration);

            // Save final agent response to memory via AgentContext delegate
            if (response != null) {
                if (response.getResponse() != null) {
                    context.saveMessage("agent", response.getResponse());
                }
                response.setRequestId(context.getRequestId());
            }

            return response;
        } catch (final Exception exception) {
            final long duration = System.currentTimeMillis() - startTime;
            metricsService.recordAgentExecution(routedAgent.getId(), "failure", duration);
            throw exception;
        }
    }
}
