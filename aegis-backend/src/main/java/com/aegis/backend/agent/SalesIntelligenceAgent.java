package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.SalesIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.SalesIntelligenceParser;
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
public class SalesIntelligenceAgent implements Agent {

    private static final String KEY_WON_AMOUNT_SUM = "wonAmountSum";

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final SalesIntelligenceParser parser;

    public SalesIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final SalesIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "sales_intelligence";
    }

    @Override
    public String getName() {
        return "Sales Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes corporate sales pipelines, forecasts quarterly deals performance, audits pipeline risks, and suggests sales strategy optimizations.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("sales analysis", "pipeline forecasting", "sales opportunity audit", "deal risk assessment");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest agentChatRequest, final AgentContext agentContext) {
        final Map<String, Object> salesToolArgs = new HashMap<>();
        final String lowercaseQuery = agentChatRequest.getMessage().toLowerCase();
        if (lowercaseQuery.contains("my") || lowercaseQuery.contains("personal")) {
            salesToolArgs.put("username", agentContext.getUsername());
        }

        Map<String, Object> aggregatedResult;
        try {
            aggregatedResult = (Map<String, Object>) toolExecutor.execute("sales_summary", salesToolArgs);
        } catch (final Exception exception) {
            log.error("Failed to execute SalesSummaryTool. Activating empty fallback context.", exception);
            aggregatedResult = new HashMap<>();
            aggregatedResult.put("totalCount", 0);
            aggregatedResult.put("openCount", 0);
            aggregatedResult.put("wonCount", 0);
            aggregatedResult.put("lostCount", 0);
            aggregatedResult.put(KEY_WON_AMOUNT_SUM, BigDecimal.ZERO);
            aggregatedResult.put("winRate", BigDecimal.ZERO);
            aggregatedResult.put("deals", Collections.emptyList());
        }

        final Integer totalCount = (Integer) aggregatedResult.getOrDefault("totalCount", 0);
        final Integer openCount = (Integer) aggregatedResult.getOrDefault("openCount", 0);
        final Integer wonCount = (Integer) aggregatedResult.getOrDefault("wonCount", 0);
        final Integer lostCount = (Integer) aggregatedResult.getOrDefault("lostCount", 0);
        final BigDecimal wonAmountSum = (BigDecimal) aggregatedResult.getOrDefault(KEY_WON_AMOUNT_SUM, BigDecimal.ZERO);
        final BigDecimal winRate = (BigDecimal) aggregatedResult.getOrDefault("winRate", BigDecimal.ZERO);
        final List<Map<String, Object>> dealsList =
                (List<Map<String, Object>>) aggregatedResult.getOrDefault("deals", Collections.emptyList());

        final String rawDealsFormatted = dealsList.stream()
                .map(item -> String.format(
                        "- Deal: %s, Amount: $%s, Status: %s, Customer: %s, Owner: %s",
                        item.getOrDefault("title", ""),
                        item.getOrDefault("amount", ""),
                        item.getOrDefault("status", ""),
                        item.getOrDefault("customerName", ""),
                        item.getOrDefault("username", "")))
                .collect(Collectors.joining("\n"));

        final String salesMetricsStr = String.format(
                "Sales Pipeline Context:\n- Total Logged Deals: %d\n"
                        + "- Open: %d, Won: %d, Lost: %d\n"
                        + "- Won Value Sum: $%s (Win Rate: %s%%)\n"
                        + "Deals Registry:\n%s\n",
                totalCount, openCount, wonCount, lostCount, wonAmountSum, winRate, rawDealsFormatted);

        final String generatedQuery = agentContext.buildQuery(salesMetricsStr, agentChatRequest.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("sales_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final SalesIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getStrategyOptimizations())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or decode sales intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = dealsList.stream()
                    .map(item -> String.format(
                            "| %s | $%s | %s | %s |",
                            item.getOrDefault("title", ""),
                            item.getOrDefault("amount", ""),
                            item.getOrDefault("status", ""),
                            item.getOrDefault("customerName", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Sales Pipeline Report (Fallback Mode)\n\n"
                            + "An anomaly occurred processing details. Sales pipeline summary remains accessible:\n\n"
                            + "**Total Logged Deals**: %d\n"
                            + "**Pipeline Stats**: Open: %d, Won: %d, Lost: %d\n"
                            + "**Won Value Sum**: $%s (Win Rate: %s%%)\n\n"
                            + "| Deal Title | Amount | Status | Customer |\n"
                            + "| :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Sales pipeline audits and strategy optimizations are currently offline.",
                    totalCount,
                    openCount,
                    wonCount,
                    lostCount,
                    wonAmountSum,
                    winRate,
                    rows.isEmpty() ? "| None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(Collections.singletonList("Retry sales pipeline audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-sales")
                    .build();
        }

        return response;
    }
}
