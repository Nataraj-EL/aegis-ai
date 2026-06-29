package com.aegis.backend.tool;

import com.aegis.backend.dto.InvoiceMetricsProjection;
import com.aegis.backend.entity.Invoice;
import com.aegis.backend.entity.InvoiceStatus;
import com.aegis.backend.repository.InvoiceRepository;
import com.aegis.backend.repository.InvoiceSpecifications;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InvoiceSummaryTool implements Tool {

    private final InvoiceRepository invoiceRepository;

    public InvoiceSummaryTool(final InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public String getId() {
        return "invoice_summary";
    }

    @Override
    public String getName() {
        return "Invoice Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates client invoicing data. Accepts optional 'status' and 'customerId' arguments.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        // Compute aggregates directly from database projections
        final List<InvoiceMetricsProjection> metrics = invoiceRepository.getInvoiceMetricsSummary();

        long totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        long draftCount = 0;
        BigDecimal draftAmount = BigDecimal.ZERO;
        long pendingCount = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        long paidCount = 0;
        BigDecimal paidAmount = BigDecimal.ZERO;
        long overdueCount = 0;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        long cancelledCount = 0;
        BigDecimal cancelledAmount = BigDecimal.ZERO;

        for (final InvoiceMetricsProjection m : metrics) {
            final InvoiceStatus s = m.getStatus();
            final long count = m.getCount() != null ? m.getCount() : 0L;
            final BigDecimal amt = m.getTotalAmount() != null ? m.getTotalAmount() : BigDecimal.ZERO;

            totalCount += count;
            totalAmount = totalAmount.add(amt);

            if (s == InvoiceStatus.DRAFT) {
                draftCount = count;
                draftAmount = amt;
            } else if (s == InvoiceStatus.PENDING) {
                pendingCount = count;
                pendingAmount = amt;
            } else if (s == InvoiceStatus.PAID) {
                paidCount = count;
                paidAmount = amt;
            } else if (s == InvoiceStatus.OVERDUE) {
                overdueCount = count;
                overdueAmount = amt;
            } else if (s == InvoiceStatus.CANCELLED) {
                cancelledCount = count;
                cancelledAmount = amt;
            }
        }

        final BigDecimal outstandingAmount = pendingAmount.add(overdueAmount);

        // Fetch detailed invoices with dynamic specifications
        Specification<Invoice> spec = Specification.where(null);
        if (arguments != null) {
            if (arguments.get("status") != null) {
                final String statusStr = (String) arguments.get("status");
                try {
                    spec = spec.and(InvoiceSpecifications.withStatus(InvoiceStatus.valueOf(statusStr.toUpperCase())));
                } catch (final Exception exception) {
                    log.warn("Invalid invoice status supplied to summary tool: {}", statusStr, exception);
                }
            }
            if (arguments.get("customerId") != null) {
                final String custIdStr = (String) arguments.get("customerId");
                try {
                    spec = spec.and(InvoiceSpecifications.withCustomerId(UUID.fromString(custIdStr)));
                } catch (final Exception exception) {
                    log.warn("Invalid customer ID supplied to summary tool: {}", custIdStr, exception);
                }
            }
        }

        final List<Invoice> invoices = invoiceRepository.findAll(spec);
        final List<Map<String, Object>> list = new ArrayList<>();
        for (final Invoice inv : invoices) {
            final Map<String, Object> map = new HashMap<>();
            map.put("id", inv.getId().toString());
            map.put("invoiceNumber", inv.getInvoiceNumber());
            map.put("amount", inv.getAmount());
            map.put("status", inv.getStatus().name());
            map.put("customerName", inv.getCustomer().getName());
            map.put("dueDate", inv.getDueDate().toString());
            list.add(map);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("totalAmount", totalAmount);
        result.put("draftCount", draftCount);
        result.put("draftAmount", draftAmount);
        result.put("pendingCount", pendingCount);
        result.put("pendingAmount", pendingAmount);
        result.put("paidCount", paidCount);
        result.put("paidAmount", paidAmount);
        result.put("overdueCount", overdueCount);
        result.put("overdueAmount", overdueAmount);
        result.put("cancelledCount", cancelledCount);
        result.put("cancelledAmount", cancelledAmount);
        result.put("outstandingAmount", outstandingAmount);
        result.put("invoices", list);
        return result;
    }
}
