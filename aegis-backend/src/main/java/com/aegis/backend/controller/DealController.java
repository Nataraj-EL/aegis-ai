package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.DealCreateRequest;
import com.aegis.backend.dto.DealResponse;
import com.aegis.backend.entity.DealStatus;
import com.aegis.backend.service.DealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Deal Controller", description = "REST endpoints for managing corporate sales deals")
@RestController
@RequestMapping("/api/v1/deals")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class DealController {

    private static final String CODE_200 = "200";

    private final DealService dealService;

    public DealController(final DealService dealService) {
        this.dealService = dealService;
    }

    @Operation(summary = "Register a new sales deal", description = "Logs a new sales deal starting under OPEN status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Deal registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid input or customer not found")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DealResponse>> createDeal(@Valid @RequestBody final DealCreateRequest request) {
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final DealResponse response = dealService.createDeal(request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Deal registered successfully"));
    }

    @Operation(
            summary = "List sales deals",
            description = "Retrieves sales deals with optional stage status, customer, and minimum amount filters.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Deals loaded successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DealResponse>>> getDeals(
            @RequestParam(required = false) final DealStatus status,
            @RequestParam(required = false) final UUID customerId,
            @RequestParam(required = false) final BigDecimal minAmount) {
        final List<DealResponse> response = dealService.getDeals(status, customerId, minAmount);
        return ResponseEntity.ok(ApiResponse.success(response, "Deals loaded successfully"));
    }

    @Operation(
            summary = "Get single sales deal details",
            description = "Retrieves details for a specific sales deal ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Deal details loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deal not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DealResponse>> getDeal(@PathVariable final UUID id) {
        final DealResponse response = dealService.getDeal(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Deal details loaded successfully"));
    }

    @Operation(
            summary = "Update sales deal status stage",
            description = "Updates the deal status (only OPEN deals can transition to CLOSED_WON or CLOSED_LOST).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Deal status updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid stage transition or deal finalized")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DealResponse>> updateDealStatus(
            @PathVariable final UUID id, @RequestParam final DealStatus status) {
        final DealResponse response = dealService.updateDealStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Deal status updated successfully"));
    }
}
