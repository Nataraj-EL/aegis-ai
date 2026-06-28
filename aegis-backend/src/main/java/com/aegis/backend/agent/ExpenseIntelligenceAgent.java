package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ExpenseIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.ExpenseIntelligenceParser;
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
public class ExpenseIntelligenceAgent implements Agent {

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final ExpenseIntelligenceParser parser;

    public ExpenseIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final ExpenseIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "expense_intelligence";
    }

    @Override
    public String getName() {
        return "Expense Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes expense reports, corporate spending ledgers, and transaction details to identify anomalies, budget risks, and cost savings opportunities.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
                "expense analysis", "spend reporting", "anomaly detection", "budget audit", "savings recommendations");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest request, final AgentContext context) {
        final Map<String, Object> toolArgs = new HashMap<>();
        final String messageLower = request.getMessage().toLowerCase();
        if (messageLower.contains("my") || messageLower.contains("personal")) {
            toolArgs.put("username", context.getUsername());
        }

        Map<String, Object> summary;
        try {
            summary = (Map<String, Object>) toolExecutor.execute("expense_summary", toolArgs);
        } catch (final Exception exception) {
            log.error("Failed to execute ExpenseSummaryTool. Falling back to default empty metrics.", exception);
            summary = new HashMap<>();
            summary.put("totalAmount", BigDecimal.ZERO);
            summary.put("categoryTotals", Collections.emptyMap());
            summary.put("statusTotals", Collections.emptyMap());
            summary.put("rawExpenses", Collections.emptyList());
        }

        final BigDecimal totalAmount = (BigDecimal) summary.getOrDefault("totalAmount", BigDecimal.ZERO);
        final Map<String, BigDecimal> categoryTotals =
                (Map<String, BigDecimal>) summary.getOrDefault("categoryTotals", Collections.emptyMap());
        final Map<String, BigDecimal> statusTotals =
                (Map<String, BigDecimal>) summary.getOrDefault("statusTotals", Collections.emptyMap());
        final List<Map<String, Object>> rawExpenses =
                (List<Map<String, Object>>) summary.getOrDefault("rawExpenses", Collections.emptyList());

        final String formattedRaw = rawExpenses.stream()
                .map(item -> String.format(
                        "- [%s] Category: %s, Amount: $%s, Status: %s, Description: %s filed by %s",
                        item.getOrDefault("createdAt", ""),
                        item.getOrDefault("category", ""),
                        item.getOrDefault("amount", ""),
                        item.getOrDefault("status", ""),
                        item.getOrDefault("description", ""),
                        item.getOrDefault("username", "")))
                .collect(Collectors.joining("\n"));

        final String metricsContext = String.format(
                "Expense Operational Context:\n- Total Logged Amount: $%s\n"
                        + "- Category Totals: %s\n"
                        + "- Lifecycle Status Totals: %s\n"
                        + "Expense Ledger Entries:\n%s\n",
                totalAmount, categoryTotals, statusTotals, formattedRaw);

        final String finalQuery = context.buildQuery(metricsContext, request.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("expense_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, finalQuery);
            final ExpenseIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getSavingsOpportunities())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or parse expense intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String catTotalsStr = categoryTotals.entrySet().stream()
                    .map(entry -> String.format("| %s | $%s |", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"));

            final String statTotalsStr = statusTotals.entrySet().stream()
                    .map(entry -> String.format("| %s | $%s |", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Expense Ledger Report (Fallback Mode)\n\n"
                            + "We encountered an anomaly compiling detailed insights. Raw spending summaries have been compiled:\n\n"
                            + "**Total Logged Claims**: $%s\n\n"
                            + "### Spending by Category\n"
                            + "| Category | Sum of Claims |\n"
                            + "| :--- | :--- |\n"
                            + "%s\n\n"
                            + "### Spending by Status\n"
                            + "| Lifecycle Status | Sum of Claims |\n"
                            + "| :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Anomalies, budget risk assessments, and saving suggestions are currently offline.",
                    totalAmount,
                    catTotalsStr.isEmpty() ? "| None | $0.00 |" : catTotalsStr,
                    statTotalsStr.isEmpty() ? "| None | $0.00 |" : statTotalsStr);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(Collections.singletonList("Retry spending audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-expenses")
                    .build();
        }

        return response;
    }
}
