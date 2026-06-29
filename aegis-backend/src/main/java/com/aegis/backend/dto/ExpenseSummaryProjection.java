package com.aegis.backend.dto;

import java.math.BigDecimal;

public interface ExpenseSummaryProjection {
    Long getCount();

    BigDecimal getTotalAmount();
}
