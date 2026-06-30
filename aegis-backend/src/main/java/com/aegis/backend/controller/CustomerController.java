package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.CustomerCreateRequest;
import com.aegis.backend.dto.CustomerResponse;
import com.aegis.backend.entity.CustomerStatus;
import com.aegis.backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Customer Controller", description = "REST endpoints for managing corporate customer accounts")
@RestController
@RequestMapping("/api/v1/customers")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class CustomerController {

    private static final String CODE_200 = "200";

    private final CustomerService customerService;

    public CustomerController(final CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Register a new customer profile", description = "Registers a new corporate customer profile.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Customer registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid input, name collision or email collision")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody final CustomerCreateRequest request) {
        final CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer registered successfully"));
    }

    @Operation(
            summary = "List customer profiles",
            description = "Retrieves customer profiles with optional status, industry, and minimum revenue filters.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Customers loaded successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCustomers(
            @RequestParam(required = false) final CustomerStatus status,
            @RequestParam(required = false) final String industry,
            @RequestParam(required = false) final BigDecimal minRevenue) {
        final List<CustomerResponse> response = customerService.getCustomers(status, industry, minRevenue);
        return ResponseEntity.ok(ApiResponse.success(response, "Customers loaded successfully"));
    }

    @Operation(
            summary = "Get single customer details",
            description = "Retrieves profile info for a specific customer ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Customer details loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable final UUID id) {
        final CustomerResponse response = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer details loaded successfully"));
    }

    @Operation(summary = "Update a customer profile", description = "Updates profile fields for a specific customer.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Customer profile updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable final UUID id, @Valid @RequestBody final CustomerCreateRequest request) {
        final CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer profile updated successfully"));
    }

    @Operation(summary = "Soft delete a customer profile", description = "Marks the customer status as INACTIVE.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Customer profile soft-deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable final UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer profile soft-deleted successfully"));
    }
}
