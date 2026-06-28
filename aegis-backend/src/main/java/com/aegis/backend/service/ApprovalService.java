package com.aegis.backend.service;

import com.aegis.backend.dto.ApprovalCreateRequest;
import com.aegis.backend.dto.ApprovalDecisionRequest;
import com.aegis.backend.dto.ApprovalResponse;
import com.aegis.backend.entity.ApprovalRequest;
import com.aegis.backend.entity.ApprovalStatus;
import com.aegis.backend.event.ApprovalStatusChangedEvent;
import com.aegis.backend.repository.ApprovalRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ApprovalService(
            final ApprovalRepository approvalRepository, final ApplicationEventPublisher eventPublisher) {
        this.approvalRepository = approvalRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ApprovalResponse createApproval(final ApprovalCreateRequest request, final String requester) {
        final ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .requester(requester)
                .approver(request.getApprover())
                .status(ApprovalStatus.PENDING)
                .build();

        final ApprovalRequest saved = approvalRepository.save(approvalRequest);
        return mapToResponse(saved);
    }

    @Transactional
    public ApprovalResponse makeDecision(final UUID id, final ApprovalDecisionRequest request, final String approver) {
        if (request.getStatus() == ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("Cannot submit PENDING as a decision status");
        }

        final ApprovalRequest approvalRequest = approvalRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found with ID: " + id));

        if (approvalRequest.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Cannot change decision on already finalized approval request");
        }

        if (!approvalRequest.getApprover().equalsIgnoreCase(approver)) {
            throw new SecurityException("Unauthorized: Only the assigned approver can make this decision");
        }

        approvalRequest.setStatus(request.getStatus());
        approvalRequest.setComments(request.getComments());

        final ApprovalRequest saved = approvalRepository.save(approvalRequest);

        // Publish decoupled status change event
        eventPublisher.publishEvent(
                new ApprovalStatusChangedEvent(this, saved.getEntityType(), saved.getEntityId(), saved.getStatus()));

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> getPendingApprovals(final String approver) {
        return approvalRepository.findByApproverAndStatus(approver, ApprovalStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ApprovalResponse mapToResponse(final ApprovalRequest request) {
        return ApprovalResponse.builder()
                .id(request.getId())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .requester(request.getRequester())
                .approver(request.getApprover())
                .status(request.getStatus())
                .comments(request.getComments())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
