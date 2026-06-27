package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentRouter {

    private final AgentRegistry agentRegistry;
    private final AiService aiService;

    public AgentRouter(final AgentRegistry agentRegistry, final AiService aiService) {
        this.agentRegistry = agentRegistry;
        this.aiService = aiService;
    }

    public Agent route(final String message) {
        final Collection<Agent> agents = agentRegistry.getAgents();

        if (agents.isEmpty()) {
            log.warn("No agents registered. Cannot route query.");
            return null;
        }

        // If only one agent is registered, skip LLM call and route directly
        if (agents.size() == 1) {
            return agents.iterator().next();
        }

        final String agentsMetadata = agents.stream()
                .map(agent -> String.format(
                        "- ID: %s, Name: %s, Description: %s, Capabilities: %s",
                        agent.getId(),
                        agent.getName(),
                        agent.getDescription(),
                        String.join(", ", agent.getCapabilities())))
                .collect(Collectors.joining("\n"));

        final String systemPrompt =
                """
                You are the routing coordinator for an Agentic Business Operating System.
                Your task is to classify the user's message and select the most appropriate Agent ID from the list of registered agents below.

                Registered Agents:
                %s

                Rules:
                1. Select the agent whose description and capabilities best fit the user's request.
                2. Reply with ONLY the exact Agent ID matching your selection (e.g., 'ceo').
                3. Do not include formatting, quotes, explanations, or any other text.
                """
                        .formatted(agentsMetadata);

        try {
            final String selectedId =
                    aiService.generateResponse(systemPrompt, message).trim().toLowerCase();
            log.info("AgentRouter classified query. Routed to Agent ID: {}", selectedId);
            final Agent selectedAgent = agentRegistry.getAgent(selectedId);
            if (selectedAgent != null) {
                return selectedAgent;
            }
            log.warn("AgentRouter returned unregistered ID: '{}'. Falling back to default CEO Agent.", selectedId);
        } catch (final Exception exception) {
            log.error("Failed to run metadata-driven AgentRouter classification", exception);
        }

        // Fallback to CEO Agent if classification fails or resolves to invalid ID
        return agentRegistry.getAgent("ceo");
    }
}
