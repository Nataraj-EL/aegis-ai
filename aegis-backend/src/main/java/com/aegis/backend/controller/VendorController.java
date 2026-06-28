package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.VendorCreateRequest;
import com.aegis.backend.dto.VendorResponse;
import com.aegis.backend.entity.VendorStatus;
import com.aegis.backend.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vendor Controller", description = "REST endpoints for managing corporate vendor profiles")
@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private static final String CODE_200 = "200";

    private final VendorService vendorService;

    public VendorController(final VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @Operation(summary = "Create a new vendor profile", description = "Registers a new corporate vendor profile.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Vendor registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid input or name collision")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<VendorResponse>> createVendor(
            @Valid @RequestBody final VendorCreateRequest request) {
        final VendorResponse response = vendorService.createVendor(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Vendor registered successfully"));
    }

    @Operation(
            summary = "List vendor profiles",
            description = "Retrieves vendor profiles with optional filtering by status, category, and minimum rating.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Vendors loaded successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorResponse>>> getVendors(
            @RequestParam(required = false) final VendorStatus status,
            @RequestParam(required = false) final String category,
            @RequestParam(required = false) final BigDecimal minRating) {
        final List<VendorResponse> response = vendorService.getVendors(status, category, minRating);
        return ResponseEntity.ok(ApiResponse.success(response, "Vendors loaded successfully"));
    }

    @Operation(summary = "Get single vendor details", description = "Retrieves profile info for a specific vendor ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Vendor details loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendor(@PathVariable final UUID id) {
        final VendorResponse response = vendorService.getVendor(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Vendor details loaded successfully"));
    }

    @Operation(summary = "Update a vendor profile", description = "Updates profile fields for a specific vendor.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Vendor profile updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendor(
            @PathVariable final UUID id, @Valid @RequestBody final VendorCreateRequest request) {
        final VendorResponse response = vendorService.updateVendor(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Vendor profile updated successfully"));
    }

    @Operation(
            summary = "Soft delete a vendor profile",
            description =
                    "Marks the vendor status as INACTIVE. Throws error if the vendor is referenced in procurement requests.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Vendor profile soft-deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Vendor is referenced by existing procurement requests")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable final UUID id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Vendor profile soft-deleted successfully"));
    }
}
