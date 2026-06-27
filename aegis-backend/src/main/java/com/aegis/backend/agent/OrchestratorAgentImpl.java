package com.aegis.backend.agent;

import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrchestratorAgentImpl implements OrchestratorAgent {

    private final AgentRouter agentRouter;

    public OrchestratorAgentImpl(final AgentRouter agentRouter) {
        this.agentRouter = agentRouter;
    }

    @Override
    public AgentChatResponse process(final AgentChatRequest request) {
        log.info("Orchestrator routing request message: {}", request.getMessage());
        final Agent routedAgent = agentRouter.route(request.getMessage());

        if (routedAgent == null) {
            log.error("No suitable executing agent resolved for request.");
            return AgentChatResponse.builder()
                    .response("Error: No suitable executing agent could be resolved to handle this request.")
                    .build();
        }

        log.info("Delegating execution process to Agent: {}", routedAgent.getName());
        return routedAgent.process(request);
    }
}
