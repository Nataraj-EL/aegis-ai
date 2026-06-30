package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.InventoryCreateRequest;
import com.aegis.backend.dto.InventoryResponse;
import com.aegis.backend.entity.InventoryStatus;
import com.aegis.backend.service.InventoryItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inventory Controller", description = "REST endpoints for managing warehouse inventory items")
@RestController
@RequestMapping("/api/v1/inventory")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class InventoryItemController {

    private static final String CODE_200 = "200";

    private final InventoryItemService inventoryItemService;

    public InventoryItemController(final InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @Operation(
            summary = "Register a new inventory item",
            description = "Logs a new product catalog item in the warehouse database.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Inventory item logged successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "SKU collision or validation failure")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventoryItem(
            @Valid @RequestBody final InventoryCreateRequest request) {
        final InventoryResponse response = inventoryItemService.createInventoryItem(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory item logged successfully"));
    }

    @Operation(
            summary = "List inventory items",
            description = "Retrieves stock listings with optional status, SKU, and maximum quantity filters.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Inventory items loaded successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryItems(
            @RequestParam(required = false) final InventoryStatus status,
            @RequestParam(required = false) final String sku,
            @RequestParam(required = false) final Integer maxQuantity) {
        final List<InventoryResponse> response = inventoryItemService.getInventoryItems(status, sku, maxQuantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory items loaded successfully"));
    }

    @Operation(
            summary = "Get single inventory item details",
            description = "Retrieves catalog info for a specific SKU ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Item details loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryItem(@PathVariable final UUID id) {
        final InventoryResponse response = inventoryItemService.getInventoryItem(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Item details loaded successfully"));
    }

    @Operation(
            summary = "Adjust inventory stock quantity",
            description =
                    "Increases or decreases warehouse stock. Rejects requests resulting in negative stock values.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Stock level adjusted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Adjustment causes negative stock or target item not found")
    })
    @PutMapping("/{id}/quantity")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateQuantity(
            @PathVariable final UUID id, @RequestParam final Integer change) {
        final InventoryResponse response = inventoryItemService.updateQuantity(id, change);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock level adjusted successfully"));
    }
}
