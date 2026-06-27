package com.aegis.backend.tool;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ToolExecutor {

    private final ToolRegistry toolRegistry;

    public ToolExecutor(final ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public Object execute(final String toolId, final Map<String, Object> arguments) {
        log.info("ToolExecutor attempting lookup and execution of Tool ID: {}", toolId);
        final Tool tool = toolRegistry.getTool(toolId);

        if (tool == null) {
            final String errorMsg = "Tool execution failure: Tool not found with ID: " + toolId;
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        try {
            log.info("Executing Tool: {} ({})", tool.getName(), tool.getId());
            return tool.execute(arguments);
        } catch (final Exception exception) {
            log.error("Error encountered executing tool with ID: {}", toolId, exception);
            throw new IllegalStateException("Failed executing tool: " + toolId, exception);
        }
    }
}
