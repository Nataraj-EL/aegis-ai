package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.BillingIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.BillingIntelligenceParser;
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
public class BillingIntelligenceAgent implements Agent {

    private static final String KEY_OUTSTANDING_TOTAL = "outstandingAmount";

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final BillingIntelligenceParser parser;

    public BillingIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final BillingIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "billing_intelligence";
    }

    @Override
    public String getName() {
        return "Billing Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes corporate invoices and billing patterns, forecasts cash flows, identifies outstanding or overdue payment risks, and suggests collection strategies.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
                "invoice analysis", "cash flow forecasting", "payment risk assessment", "collections optimization");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest billingRequest, final AgentContext billingContext) {
        final Map<String, Object> toolArguments = new HashMap<>();
        final String lowercaseQuery = billingRequest.getMessage().toLowerCase();
        if (lowercaseQuery.contains("my") || lowercaseQuery.contains("personal")) {
            toolArguments.put("username", billingContext.getUsername());
        }

        Map<String, Object> summaryResult;
        try {
            summaryResult = (Map<String, Object>) toolExecutor.execute("invoice_summary", toolArguments);
        } catch (final Exception exception) {
            log.error("Failed to execute InvoiceSummaryTool. Activating empty fallback context.", exception);
            summaryResult = new HashMap<>();
            summaryResult.put("totalCount", 0);
            summaryResult.put("draftCount", 0);
            summaryResult.put("pendingCount", 0);
            summaryResult.put("paidCount", 0);
            summaryResult.put("overdueCount", 0);
            summaryResult.put("cancelledCount", 0);
            summaryResult.put("totalAmount", BigDecimal.ZERO);
            summaryResult.put("outstandingAmount", BigDecimal.ZERO);
            summaryResult.put("invoices", Collections.emptyList());
        }

        final Integer totalCount = (Integer) summaryResult.getOrDefault("totalCount", 0);
        final Integer draftCount = (Integer) summaryResult.getOrDefault("draftCount", 0);
        final Integer pendingCount = (Integer) summaryResult.getOrDefault("pendingCount", 0);
        final Integer paidCount = (Integer) summaryResult.getOrDefault("paidCount", 0);
        final Integer overdueCount = (Integer) summaryResult.getOrDefault("overdueCount", 0);
        final Integer cancelledCount = (Integer) summaryResult.getOrDefault("cancelledCount", 0);
        final BigDecimal totalAmount = (BigDecimal) summaryResult.getOrDefault("totalAmount", BigDecimal.ZERO);
        final BigDecimal outstandingVal =
                (BigDecimal) summaryResult.getOrDefault(KEY_OUTSTANDING_TOTAL, BigDecimal.ZERO);
        final List<Map<String, Object>> invoicesList =
                (List<Map<String, Object>>) summaryResult.getOrDefault("invoices", Collections.emptyList());

        final String rawInvoicesFormatted = invoicesList.stream()
                .map(item -> String.format(
                        "- Invoice: %s, Customer: %s, Amount: $%s, Status: %s, Due: %s",
                        item.getOrDefault("invoiceNumber", ""),
                        item.getOrDefault("customerName", ""),
                        item.getOrDefault("amount", ""),
                        item.getOrDefault("status", ""),
                        item.getOrDefault("dueDate", "")))
                .collect(Collectors.joining("\n"));

        final String billingMetricsStr = String.format(
                "Corporate Invoicing Context:\n- Total Logged Invoices: %d (Sum: $%s)\n"
                        + "- Drafts: %d, Pending Invoices: %d, Paid: %d, Overdue: %d, Cancelled: %d\n"
                        + "- Outstanding Collections Balance: $%s\n"
                        + "Invoices Registry:\n%s\n",
                totalCount,
                totalAmount,
                draftCount,
                pendingCount,
                paidCount,
                overdueCount,
                cancelledCount,
                outstandingVal,
                rawInvoicesFormatted);

        final String generatedQuery = billingContext.buildQuery(billingMetricsStr, billingRequest.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("billing_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final BillingIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getCollectionStrategies())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error("Failed to generate or decode billing intelligence report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = invoicesList.stream()
                    .map(item -> String.format(
                            "| %s | %s | $%s | %s |",
                            item.getOrDefault("invoiceNumber", ""),
                            item.getOrDefault("customerName", ""),
                            item.getOrDefault("amount", ""),
                            item.getOrDefault("status", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Invoices Portfolio Report (Fallback Mode)\n\n"
                            + "An anomaly occurred processing details. Billing summary remains accessible:\n\n"
                            + "**Total Invoices**: %d (Value Sum: $%s)\n"
                            + "**Portfolio Stats**: Drafts: %d, Pending Invoices: %d, Paid: %d, Overdue: %d\n"
                            + "**Outstanding Balance**: $%s\n\n"
                            + "| Invoice Number | Customer | Amount | Status |\n"
                            + "| :--- | :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Collections optimization strategies and cash flow forecasting are currently offline.",
                    totalCount,
                    totalAmount,
                    draftCount,
                    pendingCount,
                    paidCount,
                    overdueCount,
                    outstandingVal,
                    rows.isEmpty() ? "| None | None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(
                            Collections.singletonList("Retry billing portfolio audit when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-billing")
                    .build();
        }

        return response;
    }
}
