package com.aegis.backend.event;

import com.aegis.backend.entity.DealStatus;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DealStatusChangedEvent extends ApplicationEvent {
    private final UUID dealId;
    private final UUID customerId;
    private final DealStatus status;

    public DealStatusChangedEvent(
            final Object source, final UUID dealId, final UUID customerId, final DealStatus status) {
        super(source);
        this.dealId = dealId;
        this.customerId = customerId;
        this.status = status;
    }
}
