package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ApprovalIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.ApprovalIntelligenceParser;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApprovalAgent implements Agent {

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final ApprovalIntelligenceParser parser;

    public ApprovalAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final ApprovalIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "approval_agent";
    }

    @Override
    public String getName() {
        return "Approval Agent";
    }

    @Override
    public String getDescription() {
        return "Manages and analyzes pending workflow approvals, evaluates transaction risks, priorities, and provides structured action summaries.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
                "approval list", "pending approvals audit", "approval risk assessment", "approval priorities summary");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest request, final AgentContext context) {
        final Map<String, Object> toolArgs = new HashMap<>();
        toolArgs.put("approver", context.getUsername());

        Map<String, Object> summary;
        try {
            summary = (Map<String, Object>) toolExecutor.execute("approval_summary", toolArgs);
        } catch (final Exception exception) {
            log.error("Failed to execute ApprovalTool. Falling back to default empty metrics.", exception);
            summary = new HashMap<>();
            summary.put("pendingCount", 0);
            summary.put("pendingRequests", Collections.emptyList());
        }

        final Integer pendingCount = (Integer) summary.getOrDefault("pendingCount", 0);
        final List<Map<String, Object>> pendingRequests =
                (List<Map<String, Object>>) summary.getOrDefault("pendingRequests", Collections.emptyList());

        final String formattedRaw = pendingRequests.stream()
                .map(item -> String.format(
                        "- ID: %s, Entity Type: %s, Entity ID: %s, Requester: %s, Created At: %s",
                        item.getOrDefault("id", ""),
                        item.getOrDefault("entityType", ""),
                        item.getOrDefault("entityId", ""),
                        item.getOrDefault("requester", ""),
                        item.getOrDefault("createdAt", "")))
                .collect(Collectors.joining("\n"));

        final String metricsContext = String.format(
                "Pending Approvals Operational Context:\n- Total Pending Request Items: %d\n"
                        + "Pending Approval Queue Entries:\n%s\n",
                pendingCount, formattedRaw);

        final String finalQuery = context.buildQuery(metricsContext, request.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("approval_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, finalQuery);
            final ApprovalIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getPriorityOrder())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or parse approval intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = pendingRequests.stream()
                    .map(item -> String.format(
                            "| %s | %s | %s | %s |",
                            item.getOrDefault("id", ""),
                            item.getOrDefault("entityType", ""),
                            item.getOrDefault("requester", ""),
                            item.getOrDefault("createdAt", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Pending Approvals Dashboard (Fallback Mode)\n\n"
                            + "We encountered an anomaly compiling detailed insights. Direct pending items have been recovered:\n\n"
                            + "**Total Pending Items**: %d\n\n"
                            + "| ID | Entity Type | Requester | Created At |\n"
                            + "| :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Approval prioritizations and risk audits are currently offline.",
                    pendingCount, rows.isEmpty() ? "| None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(Collections.singletonList("Retry approval queue audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-approvals")
                    .build();
        }

        return response;
    }
}
