package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ExecutiveInsightsReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.ExecutiveInsightsParser;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExecutiveInsightsAgent implements Agent {

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final ExecutiveInsightsParser insightsParser;

    public ExecutiveInsightsAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final ExecutiveInsightsParser insightsParser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.insightsParser = insightsParser;
    }

    @Override
    public String getId() {
        return "executive_insights";
    }

    @Override
    public String getName() {
        return "Executive Insights Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes business and operational data across users, documents, and audit logs to generate executive dashboard summaries and KPI assessments.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("executive insights", "business metrics", "kpis", "reporting", "operational dashboard summary");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest request, final AgentContext context) {
        Map<String, Object> metrics;
        try {
            metrics = (Map<String, Object>) toolExecutor.execute("dashboard_summary", Collections.emptyMap());
        } catch (final Exception exception) {
            log.error("Failed to execute DashboardSummaryTool. Falling back to default zeroes.", exception);
            metrics = new HashMap<>();
            metrics.put("usersCount", 0L);
            metrics.put("auditEventsCount", 0L);
            metrics.put("documentsCount", 0L);
            metrics.put("documentChunksCount", 0L);
        }

        final String metricsStr = String.format(
                "System Database Metrics:\n- Registered Users: %d\n- Recorded Audit Events: %d\n- Uploaded Documents: %d\n- Created Document Chunks: %d\n",
                metrics.getOrDefault("usersCount", 0L),
                metrics.getOrDefault("auditEventsCount", 0L),
                metrics.getOrDefault("documentsCount", 0L),
                metrics.getOrDefault("documentChunksCount", 0L));

        final String historyText = context.getFormattedHistory();
        final String contextText = context.getFormattedRetrievedContext();

        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(metricsStr).append("\n");
        if (!contextText.isEmpty()) {
            queryBuilder.append(contextText);
        }
        if (!historyText.isEmpty()) {
            queryBuilder.append("Conversation History:\n").append(historyText).append("\n");
        }
        queryBuilder.append("Current User Request:\n[USER]: ").append(request.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("executive_insights_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse chatResponse;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, queryBuilder.toString());
            final ExecutiveInsightsReport report = insightsParser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            chatResponse = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getRecommendations())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or parse executive insights. Activating fallback dashboard.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String fallbackMarkdown = String.format(
                    "# Operational Metrics Dashboard (Fallback Mode)\n\n"
                            + "We encountered a temporary processing anomaly. Direct system telemetry has been safely recovered:\n\n"
                            + "| Telemetry Entity | Measured Count |\n"
                            + "| :--- | :--- |\n"
                            + "| Registered Users | %s |\n"
                            + "| Audit Events | %s |\n"
                            + "| Ingested Documents | %s |\n"
                            + "| Document Chunks | %s |\n\n"
                            + "> [!WARNING]\n"
                            + "> Business analytics recommendations and trends are currently unavailable due to raw response decoding issues.",
                    metrics.getOrDefault("usersCount", 0L),
                    metrics.getOrDefault("auditEventsCount", 0L),
                    metrics.getOrDefault("documentsCount", 0L),
                    metrics.getOrDefault("documentChunksCount", 0L));

            chatResponse = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(Collections.singletonList("Retry analysis when LLM channel stabilizes"))
                    .executionTime(duration)
                    .model("fallback-telemetry")
                    .build();
        }

        return chatResponse;
    }
}
