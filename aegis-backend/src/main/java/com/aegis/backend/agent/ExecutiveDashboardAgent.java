package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ExecutiveDashboardReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.ExecutiveDashboardParser;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExecutiveDashboardAgent implements Agent {

    private static final String KEY_DASHBOARD_KPI = "expenseCount";
    private static final String DEFAULT_ZERO_AMOUNT = "0.00";

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final ExecutiveDashboardParser parser;

    public ExecutiveDashboardAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final ExecutiveDashboardParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "executive_dashboard";
    }

    @Override
    public String getName() {
        return "Executive Dashboard Agent";
    }

    @Override
    public String getDescription() {
        return "Compiles real-time metrics summary across Expenses, Procurement, Vendors, Customers, Sales, Inventory, Invoices, Tickets, and Knowledge Base.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
                "business health analysis",
                "cost revenue evaluation",
                "operational bottleneck assessment",
                "strategic planning suggestions");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest dashboardRequest, final AgentContext dashboardContext) {
        Map<String, Object> summaryResult;
        try {
            summaryResult = (Map<String, Object>) toolExecutor.execute("executive_dashboard", Collections.emptyMap());
        } catch (final Exception exception) {
            log.error("Failed to execute ExecutiveDashboardTool. Activating empty fallback context.", exception);
            summaryResult = Collections.emptyMap();
        }

        final Object expenseCount = summaryResult.getOrDefault(KEY_DASHBOARD_KPI, 0);
        final Object expenseTotal = summaryResult.getOrDefault("expenseTotalAmount", DEFAULT_ZERO_AMOUNT);
        final Object procurementCount = summaryResult.getOrDefault("procurementCount", 0);
        final Object procurementTotal = summaryResult.getOrDefault("procurementTotalCost", DEFAULT_ZERO_AMOUNT);
        final Object procurementPending = summaryResult.getOrDefault("procurementPendingCost", DEFAULT_ZERO_AMOUNT);
        final Object vendorCount = summaryResult.getOrDefault("vendorCount", 0);
        final Object averageRating = summaryResult.getOrDefault("averageVendorRating", 0.0);
        final Object customerCount = summaryResult.getOrDefault("customerCount", 0);
        final Object dealCount = summaryResult.getOrDefault("dealCount", 0);
        final Object dealTotal = summaryResult.getOrDefault("dealTotalValue", DEFAULT_ZERO_AMOUNT);
        final Object closedWon = summaryResult.getOrDefault("dealClosedWonValue", DEFAULT_ZERO_AMOUNT);
        final Object inventoryCount = summaryResult.getOrDefault("inventoryItemCount", 0);
        final Object inventoryValuation = summaryResult.getOrDefault("inventoryTotalValuation", DEFAULT_ZERO_AMOUNT);
        final Object invoiceCount = summaryResult.getOrDefault("invoiceCount", 0);
        final Object invoiceTotal = summaryResult.getOrDefault("invoiceTotalAmount", DEFAULT_ZERO_AMOUNT);
        final Object invoicePaid = summaryResult.getOrDefault("invoicePaidAmount", DEFAULT_ZERO_AMOUNT);
        final Object outstanding = summaryResult.getOrDefault("invoiceOutstandingAmount", DEFAULT_ZERO_AMOUNT);
        final Object ticketCount = summaryResult.getOrDefault("ticketCount", 0);
        final Object knowledgeCount = summaryResult.getOrDefault("knowledgeDocCount", 0);

        final String formattedPayload = String.format(
                "Corporate Analytics Executive Summary:\n"
                        + "- Expenses: Total of %s across %s items\n"
                        + "- Procurement: Cost of %s (Pending: %s) across %s items\n"
                        + "- Vendors: %s active partners (Avg rating: %s)\n"
                        + "- Customers: %s registered accounts\n"
                        + "- Deals/Sales: Total pipeline of %s (Closed Won: %s) across %s opportunities\n"
                        + "- Inventory: Asset valuation of %s across %s stocked items\n"
                        + "- Invoices/Billing: Total billed of %s (Paid: %s, Outstanding: %s) across %s statements\n"
                        + "- Tickets/Support: %s customer tickets filed\n"
                        + "- Knowledge Base: %s indexed document articles\n",
                expenseTotal,
                expenseCount,
                procurementTotal,
                procurementPending,
                procurementCount,
                vendorCount,
                averageRating,
                customerCount,
                dealTotal,
                closedWon,
                dealCount,
                inventoryValuation,
                inventoryCount,
                invoiceTotal,
                invoicePaid,
                outstanding,
                invoiceCount,
                ticketCount,
                knowledgeCount);

        final String generatedQuery = dashboardContext.buildQuery(formattedPayload, dashboardRequest.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("executive_dashboard_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final ExecutiveDashboardReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getStrategicSuggestions())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error(
                    "Failed to generate or decode executive intelligence report. Activating fallback summary.",
                    exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String fallbackMarkdown = String.format(
                    "# Executive Dashboard Summary (Fallback Mode)\n\n"
                            + "An error occurred compiling details. The current system metrics are:\n\n"
                            + "| Metric Category | Volume Count | Monetary Sums / Values |\n"
                            + "| :--- | :--- | :--- |\n"
                            + "| Expenses | %s | %s |\n"
                            + "| Procurement | %s | %s (Pending: %s) |\n"
                            + "| Sales Pipeline | %s | %s (Won: %s) |\n"
                            + "| Billing / Invoices | %s | %s (Paid: %s, Outstanding: %s) |\n"
                            + "| Stock Inventory | %s | %s |\n"
                            + "| Customer Partnerships | %s | - |\n"
                            + "| Registered Vendors | %s | Avg Rating: %s |\n"
                            + "| Support tickets | %s | - |\n"
                            + "| Knowledge Docs | %s | - |\n\n"
                            + "> [!WARNING]\n"
                            + "> Natural language planning suggestions and strategic narratives are currently offline.",
                    expenseCount,
                    expenseTotal,
                    procurementCount,
                    procurementTotal,
                    procurementPending,
                    dealCount,
                    dealTotal,
                    closedWon,
                    invoiceCount,
                    invoiceTotal,
                    invoicePaid,
                    outstanding,
                    inventoryCount,
                    inventoryValuation,
                    customerCount,
                    vendorCount,
                    averageRating,
                    ticketCount,
                    knowledgeCount);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(
                            Collections.singletonList("Retry dashboard analysis query when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-dashboard")
                    .build();
        }

        return response;
    }
}
