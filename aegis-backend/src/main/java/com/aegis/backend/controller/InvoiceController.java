package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.InvoiceCreateRequest;
import com.aegis.backend.dto.InvoiceResponse;
import com.aegis.backend.entity.InvoiceStatus;
import com.aegis.backend.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
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

@Tag(name = "Invoice Controller", description = "REST endpoints for managing client invoices")
@RestController
@RequestMapping("/api/v1/invoices")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class InvoiceController {

    private static final String CODE_200 = "200";

    private final InvoiceService invoiceService;

    public InvoiceController(final InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Operation(summary = "Register a new client invoice", description = "Registers a new customer invoice profile.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Invoice registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid input, deal mismatch, or invoice number collision")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody final InvoiceCreateRequest request) {
        final InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice registered successfully"));
    }

    @Operation(
            summary = "List invoices",
            description =
                    "Retrieves client invoices with optional filters for status, customer, date ranges, and minimum amounts.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Invoices loaded successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(
            @RequestParam(required = false) final InvoiceStatus status,
            @RequestParam(required = false) final UUID customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime toDate,
            @RequestParam(required = false) final BigDecimal minAmount) {
        final List<InvoiceResponse> response =
                invoiceService.getInvoices(status, customerId, fromDate, toDate, minAmount);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoices loaded successfully"));
    }

    @Operation(summary = "Get single invoice details", description = "Retrieves details for a specific invoice ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Invoice details loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable final UUID id) {
        final InvoiceResponse response = invoiceService.getInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice details loaded successfully"));
    }

    @Operation(
            summary = "Update invoice status stage",
            description = "Updates an invoice's status stage, enforcing valid workflow state transitions.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Invoice status updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid status transition or target invoice finalized")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceStatus(
            @PathVariable final UUID id, @RequestParam final InvoiceStatus status) {
        final InvoiceResponse response = invoiceService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice status updated successfully"));
    }

    @Operation(
            summary = "Soft delete/cancel a client invoice",
            description = "Transitions an invoice's status to CANCELLED.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Invoice cancelled successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable final UUID id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice cancelled successfully"));
    }
}
