package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.CustomerIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.CustomerIntelligenceParser;
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
public class CustomerIntelligenceAgent implements Agent {

    private static final String KEY_TOTAL_REVENUE = "totalRevenue";

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final CustomerIntelligenceParser parser;

    public CustomerIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final CustomerIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "customer_intelligence";
    }

    @Override
    public String getName() {
        return "Customer Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes corporate customer accounts, estimates lifecycle value and revenue contributions, audits pipeline risks, and suggests expansion or retention strategies.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("customer analysis", "revenue telemetry", "retention risk audit", "sales pipeline optimization");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest request, final AgentContext context) {
        final Map<String, Object> toolArguments = new HashMap<>();
        final String inputMessageText = request.getMessage().toLowerCase();
        if (inputMessageText.contains("my") || inputMessageText.contains("personal")) {
            toolArguments.put("username", context.getUsername());
        }

        Map<String, Object> toolResult;
        try {
            toolResult = (Map<String, Object>) toolExecutor.execute("customer_summary", toolArguments);
        } catch (final Exception exception) {
            log.error("Failed to execute CustomerSummaryTool. Activating empty fallback context.", exception);
            toolResult = new HashMap<>();
            toolResult.put("totalCount", 0);
            toolResult.put("activeCount", 0);
            toolResult.put("inactiveCount", 0);
            toolResult.put("leadCount", 0);
            toolResult.put("prospectCount", 0);
            toolResult.put(KEY_TOTAL_REVENUE, BigDecimal.ZERO);
            toolResult.put("customers", Collections.emptyList());
        }

        final Integer totalCount = (Integer) toolResult.getOrDefault("totalCount", 0);
        final Integer activeCount = (Integer) toolResult.getOrDefault("activeCount", 0);
        final Integer inactiveCount = (Integer) toolResult.getOrDefault("inactiveCount", 0);
        final Integer leadCount = (Integer) toolResult.getOrDefault("leadCount", 0);
        final Integer prospectCount = (Integer) toolResult.getOrDefault("prospectCount", 0);
        final BigDecimal totalRevenue = (BigDecimal) toolResult.getOrDefault(KEY_TOTAL_REVENUE, BigDecimal.ZERO);
        final List<Map<String, Object>> customersList =
                (List<Map<String, Object>>) toolResult.getOrDefault("customers", Collections.emptyList());

        final String rawCustomersFormatted = customersList.stream()
                .map(item -> String.format(
                        "- Account: %s, Industry: %s, Revenue: $%s, Status: %s, Email: %s",
                        item.getOrDefault("name", ""),
                        item.getOrDefault("industry", ""),
                        item.getOrDefault(KEY_TOTAL_REVENUE, ""),
                        item.getOrDefault("status", ""),
                        item.getOrDefault("contactEmail", "")))
                .collect(Collectors.joining("\n"));

        final String customerMetricsStr = String.format(
                "Client Account Analysis Context:\n- Total Catalog Accounts: %d\n"
                        + "- Lead: %d, Prospect: %d, Active: %d, Inactive: %d\n"
                        + "- Total Logged Revenue: $%s\n"
                        + "Customer Profiles:\n%s\n",
                totalCount, leadCount, prospectCount, activeCount, inactiveCount, totalRevenue, rawCustomersFormatted);

        final String generatedQuery = context.buildQuery(customerMetricsStr, request.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("customer_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final CustomerIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getExpansionStrategies())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or decode customer intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = customersList.stream()
                    .map(item -> String.format(
                            "| %s | %s | $%s | %s |",
                            item.getOrDefault("name", ""),
                            item.getOrDefault("industry", ""),
                            item.getOrDefault(KEY_TOTAL_REVENUE, ""),
                            item.getOrDefault("status", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Client Portfolio Report (Fallback Mode)\n\n"
                            + "An anomaly occurred processing details. Customer telemetry summary remains accessible:\n\n"
                            + "**Total Customer Accounts**: %d\n"
                            + "**Portfolio Stats**: Leads: %d, Prospects: %d, Active Accounts: %d, Inactive Accounts: %d\n"
                            + "**Consolidated Revenue**: $%s\n\n"
                            + "| Account Name | Industry | Revenue | Status |\n"
                            + "| :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Customer churn risk audits and sales optimization strategies are currently offline.",
                    totalCount,
                    leadCount,
                    prospectCount,
                    activeCount,
                    inactiveCount,
                    totalRevenue,
                    rows.isEmpty() ? "| None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(
                            Collections.singletonList("Retry customer portfolio audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-customers")
                    .build();
        }

        return response;
    }
}
