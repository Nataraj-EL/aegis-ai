package com.aegis.backend.tool;

import com.aegis.backend.entity.ProcurementRequest;
import com.aegis.backend.entity.ProcurementStatus;
import com.aegis.backend.repository.ProcurementRepository;
import com.aegis.backend.repository.ProcurementSpecifications;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProcurementSummaryTool implements Tool {

    private final ProcurementRepository procurementRepository;

    public ProcurementSummaryTool(final ProcurementRepository procurementRepository) {
        this.procurementRepository = procurementRepository;
    }

    @Override
    public String getId() {
        return "procurement_summary";
    }

    @Override
    public String getName() {
        return "Procurement Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates corporate procurement metrics and details. Accepts an optional 'username' parameter to filter by requester.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        final List<ProcurementRequest> requests;
        if (arguments != null && arguments.get("username") != null) {
            final String username = (String) arguments.get("username");
            requests = procurementRepository.findAll(ProcurementSpecifications.withUsername(username));
        } else {
            requests = procurementRepository.findAll();
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;

        final List<Map<String, Object>> list = new ArrayList<>();
        for (final ProcurementRequest request : requests) {
            totalCost = totalCost.add(request.getEstimatedCost());

            if (request.getStatus() == ProcurementStatus.PENDING) {
                pendingCount++;
            } else if (request.getStatus() == ProcurementStatus.APPROVED) {
                approvedCount++;
            } else if (request.getStatus() == ProcurementStatus.REJECTED) {
                rejectedCount++;
            }

            final Map<String, Object> item = new HashMap<>();
            item.put("id", request.getId().toString());
            item.put("itemName", request.getItemName());
            item.put("quantity", request.getQuantity());
            item.put("estimatedCost", request.getEstimatedCost());
            item.put("status", request.getStatus().name());
            item.put("username", request.getUsername());
            item.put("justification", request.getJustification());
            item.put(
                    "createdAt",
                    request.getCreatedAt() != null ? request.getCreatedAt().toString() : "");
            list.add(item);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCost", totalCost);
        result.put("pendingCount", pendingCount);
        result.put("approvedCount", approvedCount);
        result.put("rejectedCount", rejectedCount);
        result.put("requests", list);
        return result;
    }
}
