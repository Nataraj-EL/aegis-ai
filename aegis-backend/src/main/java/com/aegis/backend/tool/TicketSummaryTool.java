package com.aegis.backend.tool;

import com.aegis.backend.dto.TicketMetricsProjection;
import com.aegis.backend.entity.Ticket;
import com.aegis.backend.entity.TicketPriority;
import com.aegis.backend.entity.TicketStatus;
import com.aegis.backend.repository.TicketRepository;
import com.aegis.backend.repository.TicketSpecifications;
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
public class TicketSummaryTool implements Tool {

    private final TicketRepository ticketRepository;

    public TicketSummaryTool(final TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public String getId() {
        return "ticket_summary";
    }

    @Override
    public String getName() {
        return "Ticket Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates customer support tickets. Accepts optional 'status' and 'customerId' arguments.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        // Query status and priority aggregates directly from database projections
        final List<TicketMetricsProjection> metrics = ticketRepository.getTicketMetricsSummary();

        long totalCount = 0;
        final Map<String, Long> statusCounts = new HashMap<>();
        final Map<String, Long> priorityCounts = new HashMap<>();

        // Pre-populate maps with default zero values
        for (final TicketStatus status : TicketStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }
        for (final TicketPriority priority : TicketPriority.values()) {
            priorityCounts.put(priority.name(), 0L);
        }

        for (final TicketMetricsProjection m : metrics) {
            final TicketStatus s = m.getStatus();
            final TicketPriority p = m.getPriority();
            final long count = m.getCount() != null ? m.getCount() : 0L;

            if (s != null) {
                statusCounts.put(s.name(), statusCounts.get(s.name()) + count);
            }
            if (p != null) {
                priorityCounts.put(p.name(), priorityCounts.get(p.name()) + count);
            }
            totalCount += count;
        }

        // Fetch detailed tickets with dynamic specifications
        Specification<Ticket> spec = Specification.where(null);
        if (arguments != null) {
            if (arguments.get("status") != null) {
                final String statusStr = (String) arguments.get("status");
                try {
                    spec = spec.and(TicketSpecifications.withStatus(TicketStatus.valueOf(statusStr.toUpperCase())));
                } catch (final Exception exception) {
                    log.warn("Invalid ticket status supplied to summary tool: {}", statusStr, exception);
                }
            }
            if (arguments.get("customerId") != null) {
                final String custIdStr = (String) arguments.get("customerId");
                try {
                    spec = spec.and(TicketSpecifications.withCustomerId(UUID.fromString(custIdStr)));
                } catch (final Exception exception) {
                    log.warn("Invalid customer ID supplied to summary tool: {}", custIdStr, exception);
                }
            }
        }

        final List<Ticket> tickets = ticketRepository.findAll(spec);
        final List<Map<String, Object>> list = new ArrayList<>();
        for (final Ticket ticket : tickets) {
            final Map<String, Object> map = new HashMap<>();
            map.put("id", ticket.getId().toString());
            map.put("ticketNumber", ticket.getTicketNumber());
            map.put("title", ticket.getTitle());
            map.put("status", ticket.getStatus().name());
            map.put("priority", ticket.getPriority().name());
            map.put("customerName", ticket.getCustomer().getName());
            map.put("assignee", ticket.getAssignee());
            map.put(
                    "assignedAt",
                    ticket.getAssignedAt() != null ? ticket.getAssignedAt().toString() : null);
            map.put(
                    "resolvedAt",
                    ticket.getResolvedAt() != null ? ticket.getResolvedAt().toString() : null);
            list.add(map);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("statusCounts", statusCounts);
        result.put("priorityCounts", priorityCounts);
        result.put("tickets", list);
        return result;
    }
}
