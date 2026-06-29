package com.aegis.backend.dto;

import java.math.BigDecimal;

public interface InventorySummaryProjection {
    Long getCount();

    BigDecimal getTotalValuation();
}
