package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.TicketIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.TicketIntelligenceParser;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TicketIntelligenceAgent implements Agent {

    private static final String KEY_TICKETS_TOTAL = "totalCount";

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final TicketIntelligenceParser parser;

    public TicketIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final TicketIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "ticket_intelligence";
    }

    @Override
    public String getName() {
        return "Ticket Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes corporate helpdesk tickets, forecasts resolution times, identifies SLA breach or escalation risks, and suggests priority support strategies.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("ticket analysis", "resolution forecasting", "sla breach audit", "priority support plays");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest ticketRequest, final AgentContext ticketContext) {
        final Map<String, Object> toolArguments = new HashMap<>();
        final String lowercaseQuery = ticketRequest.getMessage().toLowerCase();
        if (lowercaseQuery.contains("my") || lowercaseQuery.contains("personal")) {
            toolArguments.put("username", ticketContext.getUsername());
        }

        Map<String, Object> summaryResult;
        try {
            summaryResult = (Map<String, Object>) toolExecutor.execute("ticket_summary", toolArguments);
        } catch (final Exception exception) {
            log.error("Failed to execute TicketSummaryTool. Activating empty fallback context.", exception);
            summaryResult = new HashMap<>();
            summaryResult.put("totalCount", 0L);
            summaryResult.put("statusCounts", Collections.emptyMap());
            summaryResult.put("priorityCounts", Collections.emptyMap());
            summaryResult.put("tickets", Collections.emptyList());
        }

        final Long totalCount = (Long) summaryResult.getOrDefault(KEY_TICKETS_TOTAL, 0L);
        final Map<String, Long> statusCounts =
                (Map<String, Long>) summaryResult.getOrDefault("statusCounts", Collections.emptyMap());
        final Map<String, Long> priorityCounts =
                (Map<String, Long>) summaryResult.getOrDefault("priorityCounts", Collections.emptyMap());
        final List<Map<String, Object>> ticketsList =
                (List<Map<String, Object>>) summaryResult.getOrDefault("tickets", Collections.emptyList());

        final String formattedTickets = ticketsList.stream()
                .map(item -> String.format(
                        "- Ticket: %s, Title: %s, Customer: %s, Status: %s, Priority: %s, Assignee: %s",
                        item.getOrDefault("ticketNumber", ""),
                        item.getOrDefault("title", ""),
                        item.getOrDefault("customerName", ""),
                        item.getOrDefault("status", ""),
                        item.getOrDefault("priority", ""),
                        item.getOrDefault("assignee", "")))
                .collect(Collectors.joining("\n"));

        final String statusDistStr = statusCounts.entrySet().stream()
                .map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));

        final String priorityDistStr = priorityCounts.entrySet().stream()
                .map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));

        final String ticketMetricsStr = String.format(
                "Corporate Helpdesk Context:\n- Total Support Tickets: %d\n"
                        + "- Status Distribution: %s\n"
                        + "- Priority Distribution: %s\n"
                        + "Ticket Registry Details:\n%s\n",
                totalCount, statusDistStr, priorityDistStr, formattedTickets);

        final String generatedQuery = ticketContext.buildQuery(ticketMetricsStr, ticketRequest.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("ticket_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final TicketIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getPriorityStrategies())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or decode ticket intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = ticketsList.stream()
                    .map(item -> String.format(
                            "| %s | %s | %s | %s | %s |",
                            item.getOrDefault("ticketNumber", ""),
                            item.getOrDefault("customerName", ""),
                            item.getOrDefault("title", ""),
                            item.getOrDefault("status", ""),
                            item.getOrDefault("priority", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Support Tickets Portfolio Report (Fallback Mode)\n\n"
                            + "An anomaly occurred processing details. Support ticket metrics remain accessible:\n\n"
                            + "**Total Support Tickets**: %d\n"
                            + "**Status breakdown**: %s\n"
                            + "**Priority breakdown**: %s\n\n"
                            + "| Ticket Number | Customer | Title | Status | Priority |\n"
                            + "| :--- | :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Resolution forecasting and SLA breach escalations analysis are currently offline.",
                    totalCount,
                    statusDistStr,
                    priorityDistStr,
                    rows.isEmpty() ? "| None | None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(
                            Collections.singletonList("Retry support ticket portfolio audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-tickets")
                    .build();
        }

        return response;
    }
}
