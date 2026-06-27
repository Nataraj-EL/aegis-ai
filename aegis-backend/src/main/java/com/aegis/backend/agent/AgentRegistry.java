package com.aegis.backend.agent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AgentRegistry {

    private final Map<String, Agent> registry = new ConcurrentHashMap<>();

    public AgentRegistry(final List<Agent> agents) {
        agents.forEach(this::register);
    }

    public void register(final Agent agent) {
        registry.put(agent.getId().toLowerCase(), agent);
    }

    public Agent getAgent(final String id) {
        return registry.get(id.toLowerCase());
    }

    public Collection<Agent> getAgents() {
        return registry.values();
    }
}
