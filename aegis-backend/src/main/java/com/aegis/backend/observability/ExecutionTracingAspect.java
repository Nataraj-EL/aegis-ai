package com.aegis.backend.observability;

import com.aegis.backend.agent.Agent;
import com.aegis.backend.agent.AgentContext;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Aspect
@Component
public class ExecutionTracingAspect {

    private final AuditEventPublisher auditEventPublisher;

    public ExecutionTracingAspect(final AuditEventPublisher auditEventPublisher) {
        this.auditEventPublisher = auditEventPublisher;
    }

    @Around("execution(* com.aegis.backend.agent.Agent.process(..)) && args(request, context)")
    public Object traceAgentProcess(
            final ProceedingJoinPoint joinPoint, final Object request, final AgentContext context) throws Throwable {
        final Agent agent = (Agent) joinPoint.getTarget();
        final String agentId = agent.getId();
        final String requestId = context.getRequestId();
        final String username = context.getUsername();

        log.info("Tracing Agent execution start: '{}' (RequestId: {})", agentId, requestId);
        final long startTime = System.currentTimeMillis();

        try {
            final Object result = joinPoint.proceed();
            final long duration = System.currentTimeMillis() - startTime;
            log.info("Tracing Agent execution end: '{}' successfully (Duration: {} ms)", agentId, duration);

            String metadata = "N/A";
            if (result instanceof AgentChatResponse) {
                metadata = "Model: " + ((AgentChatResponse) result).getModel();
            }

            auditEventPublisher.publishEvent(
                    requestId, username, "agent_execution:" + agentId, "SUCCESS", duration, metadata);

            return result;
        } catch (final Throwable exception) {
            final long duration = System.currentTimeMillis() - startTime;
            log.error("Tracing Agent execution failure: '{}' (Duration: {} ms)", agentId, duration, exception);

            auditEventPublisher.publishEvent(
                    requestId,
                    username,
                    "agent_execution:" + agentId,
                    "FAILURE",
                    duration,
                    truncateMessage(exception.getMessage()));

            throw exception;
        }
    }

    @Around("execution(* com.aegis.backend.tool.Tool.execute(..)) && args(arguments)")
    public Object traceToolExecute(final ProceedingJoinPoint joinPoint, final java.util.Map<String, Object> arguments)
            throws Throwable {
        final Tool tool = (Tool) joinPoint.getTarget();
        final String toolId = tool.getId();
        final String requestId = MDC.get("requestId");
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("Tracing Tool execution start: '{}' (RequestId: {})", toolId, requestId);
        final long startTime = System.currentTimeMillis();

        try {
            final Object result = joinPoint.proceed();
            final long duration = System.currentTimeMillis() - startTime;
            log.info("Tracing Tool execution end: '{}' successfully (Duration: {} ms)", toolId, duration);

            // Log key set only (never log parameter values which could contain sensitive passwords, tokens)
            auditEventPublisher.publishEvent(
                    requestId,
                    username,
                    "tool_execution:" + toolId,
                    "SUCCESS",
                    duration,
                    "Args: " + arguments.keySet());

            return result;
        } catch (final Throwable exception) {
            final long duration = System.currentTimeMillis() - startTime;
            log.error("Tracing Tool execution failure: '{}' (Duration: {} ms)", toolId, duration, exception);

            auditEventPublisher.publishEvent(
                    requestId,
                    username,
                    "tool_execution:" + toolId,
                    "FAILURE",
                    duration,
                    truncateMessage(exception.getMessage()));

            throw exception;
        }
    }

    private String truncateMessage(final String message) {
        if (message == null) {
            return null;
        }
        final int maxLength = 255;
        return message.length() > maxLength ? message.substring(0, maxLength) + "..." : message;
    }
}
