package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.DashboardSnapshotCreateRequest;
import com.aegis.backend.dto.DashboardSnapshotResponse;
import com.aegis.backend.dto.DashboardSummaryResponse;
import com.aegis.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Executive Dashboard Controller",
        description = "REST endpoints for operational KPIs summary and saved trend snapshots")
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final String CODE_200 = "200";

    private final DashboardService dashboardService;

    public DashboardController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(
            summary = "Get real-time operational summary",
            description = "Retrieves a type-safe metrics summary across all 9 modules compiled from DB aggregates.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Executive summary generated successfully")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        final DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(response, "Executive summary generated successfully"));
    }

    @Operation(
            summary = "Save a dashboard snapshot",
            description = "Stores a static JSON snapshot of the current real-time metrics summary.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Dashboard snapshot saved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid request details")
    })
    @PostMapping("/snapshots")
    public ResponseEntity<ApiResponse<DashboardSnapshotResponse>> createSnapshot(
            @Valid @RequestBody final DashboardSnapshotCreateRequest request) {
        final DashboardSnapshotResponse response = dashboardService.createSnapshot(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard snapshot saved successfully"));
    }

    @Operation(
            summary = "List dashboard snapshots",
            description = "Loads saved snapshots with optional name and date filtering.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Dashboard snapshots retrieved successfully")
    })
    @GetMapping("/snapshots")
    public ResponseEntity<ApiResponse<List<DashboardSnapshotResponse>>> getSnapshots(
            @RequestParam(required = false) final String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime after,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime before) {
        final List<DashboardSnapshotResponse> response = dashboardService.getSnapshots(title, after, before);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard snapshots retrieved successfully"));
    }

    @Operation(
            summary = "Get single dashboard snapshot",
            description = "Retrieves details for a specific dashboard snapshot ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Dashboard snapshot loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Dashboard snapshot not found")
    })
    @GetMapping("/snapshots/{id}")
    public ResponseEntity<ApiResponse<DashboardSnapshotResponse>> getSnapshotById(@PathVariable final UUID id) {
        final DashboardSnapshotResponse response = dashboardService.getSnapshotById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard snapshot loaded successfully"));
    }

    @Operation(
            summary = "Delete dashboard snapshot",
            description = "Performs a hard delete on a static saved snapshot.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Dashboard snapshot deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Dashboard snapshot not found")
    })
    @DeleteMapping("/snapshots/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSnapshot(@PathVariable final UUID id) {
        dashboardService.deleteSnapshot(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dashboard snapshot deleted successfully"));
    }
}
