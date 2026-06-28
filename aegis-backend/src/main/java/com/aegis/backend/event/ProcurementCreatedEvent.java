package com.aegis.backend.event;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProcurementCreatedEvent extends ApplicationEvent {
    private final UUID procurementId;
    private final String requester;
    private final String approver;

    public ProcurementCreatedEvent(
            final Object source, final UUID procurementId, final String requester, final String approver) {
        super(source);
        this.procurementId = procurementId;
        this.requester = requester;
        this.approver = approver;
    }
}
