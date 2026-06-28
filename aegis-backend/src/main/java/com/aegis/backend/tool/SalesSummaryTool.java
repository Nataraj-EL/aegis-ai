package com.aegis.backend.tool;

import com.aegis.backend.entity.Deal;
import com.aegis.backend.entity.DealStatus;
import com.aegis.backend.repository.DealRepository;
import com.aegis.backend.repository.DealSpecifications;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class SalesSummaryTool implements Tool {

    private final DealRepository dealRepository;

    public SalesSummaryTool(final DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public String getId() {
        return "sales_summary";
    }

    @Override
    public String getName() {
        return "Sales Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates corporate sales pipeline metrics. Accepts optional 'status' and 'customerId' arguments.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        Specification<Deal> spec = Specification.where(null);

        if (arguments != null) {
            if (arguments.get("status") != null) {
                final String statusStr = (String) arguments.get("status");
                try {
                    spec = spec.and(DealSpecifications.withStatus(DealStatus.valueOf(statusStr.toUpperCase())));
                } catch (final Exception exception) {
                    log.warn("Invalid deal status supplied to summary tool: {}", statusStr, exception);
                }
            }
            if (arguments.get("customerId") != null) {
                final String customerIdStr = (String) arguments.get("customerId");
                try {
                    spec = spec.and(DealSpecifications.withCustomerId(UUID.fromString(customerIdStr)));
                } catch (final Exception exception) {
                    log.warn("Invalid customer ID supplied to summary tool: {}", customerIdStr, exception);
                }
            }
        }

        final List<Deal> deals = dealRepository.findAll(spec);

        int totalCount = 0;
        int openCount = 0;
        int wonCount = 0;
        int lostCount = 0;
        BigDecimal wonAmountSum = BigDecimal.ZERO;

        final List<Map<String, Object>> list = new ArrayList<>();
        for (final Deal deal : deals) {
            totalCount++;
            if (deal.getStatus() == DealStatus.OPEN) {
                openCount++;
            } else if (deal.getStatus() == DealStatus.CLOSED_WON) {
                wonCount++;
                wonAmountSum = wonAmountSum.add(deal.getAmount());
            } else if (deal.getStatus() == DealStatus.CLOSED_LOST) {
                lostCount++;
            }

            final Map<String, Object> item = new HashMap<>();
            item.put("id", deal.getId().toString());
            item.put("title", deal.getTitle());
            item.put("amount", deal.getAmount());
            item.put("status", deal.getStatus().name());
            item.put(
                    "customerName",
                    deal.getCustomer() != null ? deal.getCustomer().getName() : "");
            item.put("username", deal.getUsername());
            list.add(item);
        }

        final int closedCount = wonCount + lostCount;
        final BigDecimal winRate = closedCount > 0
                ? BigDecimal.valueOf(wonCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(closedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("openCount", openCount);
        result.put("wonCount", wonCount);
        result.put("lostCount", lostCount);
        result.put("wonAmountSum", wonAmountSum);
        result.put("winRate", winRate);
        result.put("deals", list);
        return result;
    }
}
