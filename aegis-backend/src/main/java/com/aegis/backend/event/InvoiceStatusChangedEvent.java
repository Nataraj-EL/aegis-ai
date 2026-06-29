package com.aegis.backend.event;

import com.aegis.backend.entity.InvoiceStatus;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InvoiceStatusChangedEvent extends ApplicationEvent {
    private final UUID invoiceId;
    private final UUID customerId;
    private final InvoiceStatus previousStatus;
    private final InvoiceStatus currentStatus;

    public InvoiceStatusChangedEvent(
            final Object source,
            final UUID invoiceId,
            final UUID customerId,
            final InvoiceStatus previousStatus,
            final InvoiceStatus currentStatus) {
        super(source);
        this.invoiceId = invoiceId;
        this.customerId = customerId;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
    }
}
