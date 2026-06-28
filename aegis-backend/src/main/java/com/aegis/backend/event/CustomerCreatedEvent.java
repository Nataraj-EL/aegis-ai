package com.aegis.backend.event;

import com.aegis.backend.entity.CustomerStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CustomerCreatedEvent extends ApplicationEvent {
    private final UUID customerId;
    private final String customerName;
    private final CustomerStatus status;
    private final LocalDateTime createdAt;

    public CustomerCreatedEvent(
            final Object source,
            final UUID customerId,
            final String customerName,
            final CustomerStatus status,
            final LocalDateTime createdAt) {
        super(source);
        this.customerId = customerId;
        this.customerName = customerName;
        this.status = status;
        this.createdAt = createdAt;
    }
}
