package com.aegis.backend.event;

import com.aegis.backend.entity.ApprovalStatus;
import com.aegis.backend.entity.ApprovalType;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApprovalStatusChangedEvent extends ApplicationEvent {
    private final ApprovalType entityType;
    private final UUID entityId;
    private final ApprovalStatus status;

    public ApprovalStatusChangedEvent(
            final Object source, final ApprovalType entityType, final UUID entityId, final ApprovalStatus status) {
        super(source);
        this.entityType = entityType;
        this.entityId = entityId;
        this.status = status;
    }
}
