package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.VendorIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.VendorIntelligenceParser;
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
public class VendorIntelligenceAgent implements Agent {

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final VendorIntelligenceParser parser;

    public VendorIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final VendorIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "vendor_intelligence";
    }

    @Override
    public String getName() {
        return "Vendor Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes vendor performance ratings, consolidates procurement requests across vendors, audits supplier risks, and offers strategic vendor placement recommendations.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
                "vendor analysis",
                "supplier performance audit",
                "vendor consolidation",
                "strategic procurement recommendation");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest request, final AgentContext context) {
        final Map<String, Object> toolArguments = new HashMap<>();
        final String inputMessage = request.getMessage().toLowerCase();
        if (inputMessage.contains("my") || inputMessage.contains("personal")) {
            toolArguments.put("username", context.getUsername());
        }

        Map<String, Object> toolResult;
        try {
            toolResult = (Map<String, Object>) toolExecutor.execute("vendor_summary", toolArguments);
        } catch (final Exception exception) {
            log.error("Failed to execute VendorSummaryTool. Activating empty fallback context.", exception);
            toolResult = new HashMap<>();
            toolResult.put("totalCount", 0);
            toolResult.put("activeCount", 0);
            toolResult.put("inactiveCount", 0);
            toolResult.put("underReviewCount", 0);
            toolResult.put("averageRating", BigDecimal.ZERO);
            toolResult.put("vendors", Collections.emptyList());
        }

        final Integer totalCount = (Integer) toolResult.getOrDefault("totalCount", 0);
        final Integer activeCount = (Integer) toolResult.getOrDefault("activeCount", 0);
        final Integer inactiveCount = (Integer) toolResult.getOrDefault("inactiveCount", 0);
        final Integer underReviewCount = (Integer) toolResult.getOrDefault("underReviewCount", 0);
        final BigDecimal averageRating = (BigDecimal) toolResult.getOrDefault("averageRating", BigDecimal.ZERO);
        final List<Map<String, Object>> vendorsList =
                (List<Map<String, Object>>) toolResult.getOrDefault("vendors", Collections.emptyList());

        final String rawVendorsFormatted = vendorsList.stream()
                .map(item -> String.format(
                        "- Supplier: %s, Category: %s, Rating: %s, Status: %s, Email: %s",
                        item.getOrDefault("name", ""),
                        item.getOrDefault("category", ""),
                        item.getOrDefault("rating", ""),
                        item.getOrDefault("status", ""),
                        item.getOrDefault("contactEmail", "")))
                .collect(Collectors.joining("\n"));

        final String vendorMetricsStr = String.format(
                "Supplier Profile Analysis Context:\n- Total Catalog Vendors: %d\n"
                        + "- Active: %d, Inactive: %d, Under Review: %d\n"
                        + "- Mean Supplier Rating: %s/5.00\n"
                        + "Vendor Profiles:\n%s\n",
                totalCount, activeCount, inactiveCount, underReviewCount, averageRating, rawVendorsFormatted);

        final String generatedQuery = context.buildQuery(vendorMetricsStr, request.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("vendor_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final VendorIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getRecommendations())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or decode vendor intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = vendorsList.stream()
                    .map(item -> String.format(
                            "| %s | %s | %s/5.00 | %s |",
                            item.getOrDefault("name", ""),
                            item.getOrDefault("category", ""),
                            item.getOrDefault("rating", ""),
                            item.getOrDefault("status", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Supplier Catalog Report (Fallback Mode)\n\n"
                            + "An anomaly occurred processing details. Vendor telemetry summary remains accessible:\n\n"
                            + "**Total Suppliers**: %d\n"
                            + "**Performance Index**: Active: %d, Inactive: %d, Average Rating: %s\n\n"
                            + "| Supplier Name | Category | Rating | Status |\n"
                            + "| :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Supplier risk assessments and strategic placements are currently offline.",
                    totalCount,
                    activeCount,
                    inactiveCount,
                    averageRating,
                    rows.isEmpty() ? "| None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(Collections.singletonList("Retry vendor audit when LLM channel settles"))
                    .executionTime(duration)
                    .model("fallback-vendors")
                    .build();
        }

        return response;
    }
}
