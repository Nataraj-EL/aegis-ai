package com.aegis.backend.dto;

import com.aegis.backend.entity.TicketPriority;
import com.aegis.backend.entity.TicketStatus;

public interface TicketMetricsProjection {
    TicketStatus getStatus();

    TicketPriority getPriority();

    Long getCount();
}
