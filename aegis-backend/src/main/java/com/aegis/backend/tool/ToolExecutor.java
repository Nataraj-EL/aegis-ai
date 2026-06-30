package com.aegis.backend.tool;

import com.aegis.backend.service.MetricsService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ToolExecutor {

    private final ToolRegistry toolRegistry;
    private final MetricsService metricsService;

    public ToolExecutor(final ToolRegistry toolRegistry, final MetricsService metricsService) {
        this.toolRegistry = toolRegistry;
        this.metricsService = metricsService;
    }

    public Object execute(final String toolId, final Map<String, Object> arguments) {
        log.info("ToolExecutor attempting lookup and execution of Tool ID: {}", toolId);
        final Tool tool = toolRegistry.getTool(toolId);

        if (tool == null) {
            final String errorMsg = "Tool execution failure: Tool not found with ID: " + toolId;
            log.error(errorMsg);
            metricsService.recordToolExecution(toolId, "failure", 0L);
            throw new IllegalArgumentException(errorMsg);
        }

        final long startTime = System.currentTimeMillis();
        try {
            log.info("Executing Tool: {} ({})", tool.getName(), tool.getId());
            final Object result = tool.execute(arguments);
            final long duration = System.currentTimeMillis() - startTime;
            metricsService.recordToolExecution(toolId, "success", duration);
            return result;
        } catch (final Exception exception) {
            final long duration = System.currentTimeMillis() - startTime;
            log.error("Error encountered executing tool with ID: {}", toolId, exception);
            metricsService.recordToolExecution(toolId, "failure", duration);
            throw new IllegalStateException("Failed executing tool: " + toolId, exception);
        }
    }
}
