package com.aegis.backend.tool;

import com.aegis.backend.dto.DashboardSummaryResponse;
import com.aegis.backend.service.DashboardService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecutiveDashboardTool implements Tool {

    private final DashboardService dashboardService;

    public ExecutiveDashboardTool(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public String getId() {
        return "executive_dashboard";
    }

    @Override
    public String getName() {
        return "Executive Dashboard Tool";
    }

    @Override
    public String getDescription() {
        return "Compiles real-time metrics summary across Expenses, Procurement, Vendors, Customers, Sales, Inventory, Invoices, Tickets, and Knowledge Base.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        log.info("Executing Executive Dashboard Tool to gather cross-module summary metrics...");
        try {
            final DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
            final Map<String, Object> result = new HashMap<>();
            result.put("expenseCount", summary.getExpenseCount());
            result.put("expenseTotalAmount", summary.getExpenseTotalAmount().toString());
            result.put("procurementCount", summary.getProcurementCount());
            result.put("procurementTotalCost", summary.getProcurementTotalCost().toString());
            result.put(
                    "procurementPendingCost",
                    summary.getProcurementPendingCost().toString());
            result.put("vendorCount", summary.getVendorCount());
            result.put("averageVendorRating", summary.getAverageVendorRating());
            result.put("customerCount", summary.getCustomerCount());
            result.put("dealCount", summary.getDealCount());
            result.put("dealTotalValue", summary.getDealTotalValue().toString());
            result.put("dealClosedWonValue", summary.getDealClosedWonValue().toString());
            result.put("inventoryItemCount", summary.getInventoryItemCount());
            result.put(
                    "inventoryTotalValuation",
                    summary.getInventoryTotalValuation().toString());
            result.put("invoiceCount", summary.getInvoiceCount());
            result.put("invoiceTotalAmount", summary.getInvoiceTotalAmount().toString());
            result.put("invoicePaidAmount", summary.getInvoicePaidAmount().toString());
            result.put(
                    "invoiceOutstandingAmount",
                    summary.getInvoiceOutstandingAmount().toString());
            result.put("ticketCount", summary.getTicketCount());
            result.put("knowledgeDocCount", summary.getKnowledgeDocCount());
            result.put("createdAt", summary.getCreatedAt().toString());
            return result;
        } catch (final Exception exception) {
            log.error("Failed to gather executive summary metrics inside Tool.", exception);
            return Collections.emptyMap();
        }
    }
}
