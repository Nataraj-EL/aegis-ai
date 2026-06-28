package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ProcurementIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.ProcurementIntelligenceParser;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcurementIntelligenceAgent implements Agent {

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final ProcurementIntelligenceParser parser;

    public ProcurementIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final ProcurementIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "procurement_intelligence";
    }

    @Override
    public String getName() {
        return "Procurement Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes corporate procurement requests, estimates purchase costs, flags high-value acquisitions, evaluates budget risks, and suggests vendor optimization strategies.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("procurement analysis", "purchase telemetry", "vendor optimization", "procurement risk audit");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest request, final AgentContext context) {
        final Map<String, Object> toolArgs = new HashMap<>();
        final String requestQuery = request.getMessage().toLowerCase();
        if (requestQuery.contains("my") || requestQuery.contains("personal")) {
            toolArgs.put("username", context.getUsername());
        }

        Map<String, Object> summary;
        try {
            summary = (Map<String, Object>) toolExecutor.execute("procurement_summary", toolArgs);
        } catch (final Exception exception) {
            log.error("Failed to execute ProcurementSummaryTool. Falling back to default empty metrics.", exception);
            summary = new HashMap<>();
            summary.put("totalCost", BigDecimal.ZERO);
            summary.put("pendingCount", 0);
            summary.put("approvedCount", 0);
            summary.put("rejectedCount", 0);
            summary.put("requests", Collections.emptyList());
        }

        final BigDecimal totalCost = (BigDecimal) summary.getOrDefault("totalCost", BigDecimal.ZERO);
        final Integer pendingCount = (Integer) summary.getOrDefault("pendingCount", 0);
        final Integer approvedCount = (Integer) summary.getOrDefault("approvedCount", 0);
        final Integer rejectedCount = (Integer) summary.getOrDefault("rejectedCount", 0);
        final List<Map<String, Object>> requests =
                (List<Map<String, Object>>) summary.getOrDefault("requests", Collections.emptyList());

        final String formattedRaw = requests.stream()
                .map(item -> String.format(
                        "- Item: %s, Quantity: %s, Cost: $%s, Status: %s, Justification: %s filed by %s",
                        item.getOrDefault("itemName", ""),
                        item.getOrDefault("quantity", ""),
                        item.getOrDefault("estimatedCost", ""),
                        item.getOrDefault("status", ""),
                        item.getOrDefault("justification", ""),
                        item.getOrDefault("username", "")))
                .collect(Collectors.joining("\n"));

        final String metricsContext = String.format(
                "Procurement Operational Context:\n- Total Estimated Claims Cost: $%s\n"
                        + "- Pending: %d, Approved: %d, Rejected: %d\n"
                        + "Procurement claims entries:\n%s\n",
                totalCost, pendingCount, approvedCount, rejectedCount, formattedRaw);

        final String finalQuery = context.buildQuery(metricsContext, request.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("procurement_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, finalQuery);
            final ProcurementIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getVendorOptimizations())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or parse procurement intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = requests.stream()
                    .map(item -> String.format(
                            "| %s | %s | $%s | %s |",
                            item.getOrDefault("itemName", ""),
                            item.getOrDefault("quantity", ""),
                            item.getOrDefault("estimatedCost", ""),
                            item.getOrDefault("status", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Procurement Claims Dashboard (Fallback Mode)\n\n"
                            + "We encountered an anomaly compiling detailed insights. Direct metrics have been safely recovered:\n\n"
                            + "**Total Estimated Claims Cost**: $%s\n"
                            + "**Queue Stats**: Pending: %d, Approved: %d, Rejected: %d\n\n"
                            + "| Item Name | Quantity | Estimated Cost | Status |\n"
                            + "| :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Procurement risk assessments and vendor optimizations are currently offline.",
                    totalCost,
                    pendingCount,
                    approvedCount,
                    rejectedCount,
                    rows.isEmpty() ? "| None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(
                            Collections.singletonList("Retry procurement queue audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-procurements")
                    .build();
        }

        return response;
    }
}
