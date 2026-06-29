package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.InventoryIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.InventoryIntelligenceParser;
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
public class InventoryIntelligenceAgent implements Agent {

    private static final String KEY_VALUATION_TOTAL = "assetValuationSum";

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final InventoryIntelligenceParser parser;

    public InventoryIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final InventoryIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "inventory_intelligence";
    }

    @Override
    public String getName() {
        return "Inventory Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes corporate warehouse inventory levels, identifies low stock or stockout risks, monitors asset valuation, and recommends reorder schedules.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("inventory analysis", "stockout risk assessment", "asset valuation", "reorder optimization");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest chatReq, final AgentContext agentCtx) {
        final Map<String, Object> inventoryToolArgs = new HashMap<>();
        final String lowercaseMessage = chatReq.getMessage().toLowerCase();
        if (lowercaseMessage.contains("my") || lowercaseMessage.contains("personal")) {
            inventoryToolArgs.put("username", agentCtx.getUsername());
        }

        Map<String, Object> toolResult;
        try {
            toolResult = (Map<String, Object>) toolExecutor.execute("inventory_summary", inventoryToolArgs);
        } catch (final Exception exception) {
            log.error("Failed to execute InventorySummaryTool. Activating empty fallback context.", exception);
            toolResult = new HashMap<>();
            toolResult.put("totalCount", 0);
            toolResult.put("inStockCount", 0);
            toolResult.put("lowStockCount", 0);
            toolResult.put("outOfStockCount", 0);
            toolResult.put(KEY_VALUATION_TOTAL, BigDecimal.ZERO);
            toolResult.put("items", Collections.emptyList());
        }

        final Integer totalCount = (Integer) toolResult.getOrDefault("totalCount", 0);
        final Integer inStockCount = (Integer) toolResult.getOrDefault("inStockCount", 0);
        final Integer lowStockCount = (Integer) toolResult.getOrDefault("lowStockCount", 0);
        final Integer outOfStockCount = (Integer) toolResult.getOrDefault("outOfStockCount", 0);
        final BigDecimal valuationSum = (BigDecimal) toolResult.getOrDefault(KEY_VALUATION_TOTAL, BigDecimal.ZERO);
        final List<Map<String, Object>> itemsList =
                (List<Map<String, Object>>) toolResult.getOrDefault("items", Collections.emptyList());

        final String rawItemsFormatted = itemsList.stream()
                .map(item -> String.format(
                        "- SKU: %s, Item: %s, Quantity: %d, Threshold: %d, Unit Price: $%s, Status: %s",
                        item.getOrDefault("sku", ""),
                        item.getOrDefault("name", ""),
                        item.getOrDefault("quantity", 0),
                        item.getOrDefault("reorderThreshold", 0),
                        item.getOrDefault("unitPrice", ""),
                        item.getOrDefault("status", "")))
                .collect(Collectors.joining("\n"));

        final String inventoryMetricsStr = String.format(
                "Warehouse Inventory Context:\n- Total Catalog SKUs: %d\n"
                        + "- In Stock: %d, Low Stock: %d, Out of Stock: %d\n"
                        + "- Asset Valuation Sum: $%s\n"
                        + "Product Registry:\n%s\n",
                totalCount, inStockCount, lowStockCount, outOfStockCount, valuationSum, rawItemsFormatted);

        final String generatedQuery = agentCtx.buildQuery(inventoryMetricsStr, chatReq.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("inventory_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final InventoryIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getReorderRecommendations())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or decode inventory intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = itemsList.stream()
                    .map(item -> String.format(
                            "| %s | %s | %d | $%s | %s |",
                            item.getOrDefault("sku", ""),
                            item.getOrDefault("name", ""),
                            item.getOrDefault("quantity", 0),
                            item.getOrDefault("unitPrice", ""),
                            item.getOrDefault("status", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Warehouse Inventory Report (Fallback Mode)\n\n"
                            + "An anomaly occurred processing details. Inventory summary remains accessible:\n\n"
                            + "**Total Product SKUs**: %d\n"
                            + "**Inventory Stats**: In Stock: %d, Low Stock: %d, Out of Stock: %d\n"
                            + "**Asset Valuation Sum**: $%s\n\n"
                            + "| SKU | Item Name | Quantity | Price | Status |\n"
                            + "| :--- | :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Stockout risk audits and reorder optimizations are currently offline.",
                    totalCount,
                    inStockCount,
                    lowStockCount,
                    outOfStockCount,
                    valuationSum,
                    rows.isEmpty() ? "| None | None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(Collections.singletonList("Retry inventory levels audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-inventory")
                    .build();
        }

        return response;
    }
}
