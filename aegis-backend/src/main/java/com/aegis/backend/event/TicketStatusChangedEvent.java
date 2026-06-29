package com.aegis.backend.event;

import com.aegis.backend.entity.TicketStatus;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TicketStatusChangedEvent extends ApplicationEvent {
    private final UUID ticketId;
    private final UUID customerId;
    private final TicketStatus previousStatus;
    private final TicketStatus currentStatus;

    public TicketStatusChangedEvent(
            final Object source,
            final UUID ticketId,
            final UUID customerId,
            final TicketStatus previousStatus,
            final TicketStatus currentStatus) {
        super(source);
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
    }
}
