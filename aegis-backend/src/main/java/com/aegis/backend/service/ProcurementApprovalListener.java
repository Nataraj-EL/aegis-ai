package com.aegis.backend.service;

import com.aegis.backend.entity.ApprovalStatus;
import com.aegis.backend.entity.ApprovalType;
import com.aegis.backend.entity.ProcurementRequest;
import com.aegis.backend.entity.ProcurementStatus;
import com.aegis.backend.event.ApprovalStatusChangedEvent;
import com.aegis.backend.repository.ProcurementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcurementApprovalListener {

    private final ProcurementRepository procurementRepository;

    public ProcurementApprovalListener(final ProcurementRepository procurementRepository) {
        this.procurementRepository = procurementRepository;
    }

    @EventListener
    public void onApprovalStatusChanged(final ApprovalStatusChangedEvent event) {
        if (event.getEntityType() == ApprovalType.PROCUREMENT) {
            log.info("Processing procurement approval update event for Procurement ID: {}", event.getEntityId());
            final ProcurementRequest request =
                    procurementRepository.findById(event.getEntityId()).orElse(null);
            if (request == null) {
                log.error("ProcurementRequest not found for ID: {}", event.getEntityId());
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                request.setStatus(ProcurementStatus.APPROVED);
            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                request.setStatus(ProcurementStatus.REJECTED);
            }
            procurementRepository.save(request);
            log.info("ProcurementRequest status updated to: {} for ID: {}", request.getStatus(), request.getId());
        }
    }
}
