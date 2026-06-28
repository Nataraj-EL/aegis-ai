package com.aegis.backend.tool;

import com.aegis.backend.entity.Vendor;
import com.aegis.backend.entity.VendorStatus;
import com.aegis.backend.repository.VendorRepository;
import com.aegis.backend.repository.VendorSpecifications;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class VendorSummaryTool implements Tool {

    private final VendorRepository vendorRepository;

    public VendorSummaryTool(final VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    public String getId() {
        return "vendor_summary";
    }

    @Override
    public String getName() {
        return "Vendor Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates corporate vendor telemetry. Accepts optional 'status' and 'category' arguments.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        Specification<Vendor> spec = Specification.where(null);

        if (arguments != null) {
            if (arguments.get("status") != null) {
                final String statusStr = (String) arguments.get("status");
                try {
                    spec = spec.and(VendorSpecifications.withStatus(VendorStatus.valueOf(statusStr.toUpperCase())));
                } catch (final Exception exception) {
                    log.warn("Invalid vendor status supplied to summary tool: {}", statusStr, exception);
                }
            }
            if (arguments.get("category") != null) {
                spec = spec.and(VendorSpecifications.withCategory((String) arguments.get("category")));
            }
        }

        final List<Vendor> vendors = vendorRepository.findAll(spec);

        int totalCount = 0;
        int activeCount = 0;
        int inactiveCount = 0;
        int underReviewCount = 0;
        BigDecimal ratingSum = BigDecimal.ZERO;
        int ratingCount = 0;

        final List<Map<String, Object>> list = new ArrayList<>();
        for (final Vendor vendor : vendors) {
            totalCount++;
            if (vendor.getStatus() == VendorStatus.ACTIVE) {
                activeCount++;
            } else if (vendor.getStatus() == VendorStatus.INACTIVE) {
                inactiveCount++;
            } else if (vendor.getStatus() == VendorStatus.UNDER_REVIEW) {
                underReviewCount++;
            }

            if (vendor.getRating() != null) {
                ratingSum = ratingSum.add(vendor.getRating());
                ratingCount++;
            }

            final Map<String, Object> item = new HashMap<>();
            item.put("id", vendor.getId().toString());
            item.put("name", vendor.getName());
            item.put("contactEmail", vendor.getContactEmail() != null ? vendor.getContactEmail() : "");
            item.put("category", vendor.getCategory() != null ? vendor.getCategory() : "");
            item.put("status", vendor.getStatus().name());
            item.put("rating", vendor.getRating() != null ? vendor.getRating() : BigDecimal.ZERO);
            list.add(item);
        }

        final BigDecimal averageRating = ratingCount > 0
                ? ratingSum.divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("activeCount", activeCount);
        result.put("inactiveCount", inactiveCount);
        result.put("underReviewCount", underReviewCount);
        result.put("averageRating", averageRating);
        result.put("vendors", list);
        return result;
    }
}
