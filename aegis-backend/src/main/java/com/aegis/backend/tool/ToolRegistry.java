package com.aegis.backend.tool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {

    private final Map<String, Tool> registry = new ConcurrentHashMap<>();

    public ToolRegistry(final List<Tool> tools) {
        tools.forEach(this::register);
    }

    public void register(final Tool tool) {
        registry.put(tool.getId().toLowerCase(), tool);
    }

    public Tool getTool(final String id) {
        return registry.get(id.toLowerCase());
    }

    public Collection<Tool> getTools() {
        return registry.values();
    }
}
