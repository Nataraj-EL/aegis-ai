package com.aegis.backend.dto;

import com.aegis.backend.entity.InvoiceStatus;
import java.math.BigDecimal;

public interface InvoiceMetricsProjection {
    InvoiceStatus getStatus();

    Long getCount();

    BigDecimal getTotalAmount();
}
