package com.aegis.backend.dto;

import java.math.BigDecimal;

public interface DealSummaryProjection {
    Long getCount();

    BigDecimal getTotalValue();

    BigDecimal getClosedWonValue();
}
