package com.aegis.backend.dto;

import java.math.BigDecimal;

public interface InvoiceSummaryProjection {
    Long getCount();

    BigDecimal getTotalAmount();

    BigDecimal getPaidAmount();

    BigDecimal getOutstandingAmount();
}
