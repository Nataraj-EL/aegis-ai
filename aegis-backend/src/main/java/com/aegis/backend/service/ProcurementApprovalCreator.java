package com.aegis.backend.service;

import com.aegis.backend.dto.ApprovalCreateRequest;
import com.aegis.backend.entity.ApprovalType;
import com.aegis.backend.event.ProcurementCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcurementApprovalCreator {

    private final ApprovalService approvalService;

    public ProcurementApprovalCreator(final ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @EventListener
    public void onProcurementCreated(final ProcurementCreatedEvent event) {
        log.info(
                "ProcurementCreatedEvent captured for ID: {}. Triggering ApprovalRequest registration.",
                event.getProcurementId());

        final ApprovalCreateRequest approvalRequest = ApprovalCreateRequest.builder()
                .entityType(ApprovalType.PROCUREMENT)
                .entityId(event.getProcurementId())
                .approver(event.getApprover())
                .build();

        approvalService.createApproval(approvalRequest, event.getRequester());
        log.info("ApprovalRequest successfully registered for Procurement ID: {}", event.getProcurementId());
    }
}
