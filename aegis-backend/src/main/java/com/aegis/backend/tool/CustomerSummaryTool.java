package com.aegis.backend.tool;

import com.aegis.backend.entity.Customer;
import com.aegis.backend.entity.CustomerStatus;
import com.aegis.backend.repository.CustomerRepository;
import com.aegis.backend.repository.CustomerSpecifications;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerSummaryTool implements Tool {

    private final CustomerRepository customerRepository;

    public CustomerSummaryTool(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public String getId() {
        return "customer_summary";
    }

    @Override
    public String getName() {
        return "Customer Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates corporate customer account telemetry. Accepts optional 'status' and 'industry' arguments.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        Specification<Customer> spec = Specification.where(null);

        if (arguments != null) {
            if (arguments.get("status") != null) {
                final String statusStr = (String) arguments.get("status");
                try {
                    spec = spec.and(CustomerSpecifications.withStatus(CustomerStatus.valueOf(statusStr.toUpperCase())));
                } catch (final Exception exception) {
                    log.warn("Invalid customer status supplied to summary tool: {}", statusStr, exception);
                }
            }
            if (arguments.get("industry") != null) {
                spec = spec.and(CustomerSpecifications.withIndustry((String) arguments.get("industry")));
            }
        }

        final List<Customer> customers = customerRepository.findAll(spec);

        int totalCount = 0;
        int activeCount = 0;
        int inactiveCount = 0;
        int leadCount = 0;
        int prospectCount = 0;
        BigDecimal revenueSum = BigDecimal.ZERO;

        final List<Map<String, Object>> list = new ArrayList<>();
        for (final Customer customer : customers) {
            totalCount++;
            if (customer.getStatus() == CustomerStatus.ACTIVE) {
                activeCount++;
            } else if (customer.getStatus() == CustomerStatus.INACTIVE) {
                inactiveCount++;
            } else if (customer.getStatus() == CustomerStatus.LEAD) {
                leadCount++;
            } else if (customer.getStatus() == CustomerStatus.PROSPECT) {
                prospectCount++;
            }

            if (customer.getTotalRevenue() != null) {
                revenueSum = revenueSum.add(customer.getTotalRevenue());
            }

            final Map<String, Object> item = new HashMap<>();
            item.put("id", customer.getId().toString());
            item.put("name", customer.getName());
            item.put("contactEmail", customer.getContactEmail() != null ? customer.getContactEmail() : "");
            item.put("industry", customer.getIndustry() != null ? customer.getIndustry() : "");
            item.put("status", customer.getStatus().name());
            item.put("totalRevenue", customer.getTotalRevenue() != null ? customer.getTotalRevenue() : BigDecimal.ZERO);
            list.add(item);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("activeCount", activeCount);
        result.put("inactiveCount", inactiveCount);
        result.put("leadCount", leadCount);
        result.put("prospectCount", prospectCount);
        result.put("totalRevenue", revenueSum);
        result.put("customers", list);
        return result;
    }
}
