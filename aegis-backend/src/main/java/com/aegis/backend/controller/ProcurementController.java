package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.ProcurementCreateRequest;
import com.aegis.backend.dto.ProcurementResponse;
import com.aegis.backend.entity.ProcurementStatus;
import com.aegis.backend.service.ProcurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Procurement Controller", description = "REST endpoints for logging and searching procurement claims")
@RestController
@RequestMapping("/api/v1/procurements")
public class ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(final ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @Operation(summary = "Submit a procurement request", description = "Logs a purchase request under PENDING status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Procurement request submitted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProcurementResponse>> createProcurement(
            @Valid @RequestBody final ProcurementCreateRequest request) {
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final ProcurementResponse response = procurementService.createProcurement(request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Procurement request submitted successfully"));
    }

    @Operation(
            summary = "List procurement requests",
            description =
                    "Retrieves purchase requests logged by the authenticated user with optional status and cost range filters.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Procurement requests retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcurementResponse>>> getProcurements(
            @RequestParam(required = false) final ProcurementStatus status,
            @RequestParam(required = false) final BigDecimal minCost,
            @RequestParam(required = false) final BigDecimal maxCost) {
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final List<ProcurementResponse> response =
                procurementService.getProcurements(username, status, minCost, maxCost);
        return ResponseEntity.ok(ApiResponse.success(response, "Procurement requests loaded successfully"));
    }
}
