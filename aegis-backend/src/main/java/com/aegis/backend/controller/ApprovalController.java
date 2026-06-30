package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.ApprovalCreateRequest;
import com.aegis.backend.dto.ApprovalDecisionRequest;
import com.aegis.backend.dto.ApprovalResponse;
import com.aegis.backend.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Approval Controller", description = "REST endpoints for managing workflow approval requests")
@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(final ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Operation(
            summary = "Create an approval request",
            description = "Submits a new workflow item for approval, assigning it to a specific user.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Approval request created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalResponse>> createApproval(
            @Valid @RequestBody final ApprovalCreateRequest request) {
        final String requester =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final ApprovalResponse response = approvalService.createApproval(request, requester);
        return ResponseEntity.ok(ApiResponse.success(response, "Approval request registered successfully"));
    }

    @Operation(
            summary = "Submit decision on approval request",
            description = "Approves or rejects a pending request. Only the assigned approver is authorized.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Approval decision successfully saved and processed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Unauthorized request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid request or status validation failed")
    })
    @PutMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalResponse>> makeDecision(
            @PathVariable final UUID id, @Valid @RequestBody final ApprovalDecisionRequest request) {
        final String approver =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final ApprovalResponse response = approvalService.makeDecision(id, request, approver);
        return ResponseEntity.ok(ApiResponse.success(response, "Decision registered successfully"));
    }

    @Operation(
            summary = "List pending approvals",
            description =
                    "Retrieves all approval requests currently assigned to the authenticated user under PENDING status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Pending approvals retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getPendingApprovals() {
        final String approver =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final List<ApprovalResponse> response = approvalService.getPendingApprovals(approver);
        return ResponseEntity.ok(ApiResponse.success(response, "Pending approvals loaded successfully"));
    }
}
