package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.TicketCreateRequest;
import com.aegis.backend.dto.TicketResponse;
import com.aegis.backend.entity.TicketPriority;
import com.aegis.backend.entity.TicketStatus;
import com.aegis.backend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Ticket Controller", description = "REST endpoints for managing helpdesk support tickets")
@RestController
@RequestMapping("/api/v1/tickets")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class TicketController {

    private static final String CODE_200 = "200";

    private final TicketService ticketService;

    public TicketController(final TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(summary = "Register a support ticket", description = "Registers a new customer support ticket.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Support ticket registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid input or customer not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody final TicketCreateRequest request) {
        final TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Support ticket registered successfully"));
    }

    @Operation(
            summary = "List support tickets",
            description =
                    "Retrieves support tickets with optional filters for status, priority, customer, assignee, and date ranges.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Support tickets loaded successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTickets(
            @RequestParam(required = false) final TicketStatus status,
            @RequestParam(required = false) final TicketPriority priority,
            @RequestParam(required = false) final UUID customerId,
            @RequestParam(required = false) final String assignee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime toDate) {
        final List<TicketResponse> response =
                ticketService.getTickets(status, priority, customerId, assignee, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Support tickets loaded successfully"));
    }

    @Operation(summary = "Get support ticket details", description = "Retrieves details for a specific ticket ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Support ticket details loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Support ticket not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable final UUID id) {
        final TicketResponse response = ticketService.getTicket(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Support ticket details loaded successfully"));
    }

    @Operation(
            summary = "Update support ticket status",
            description =
                    "Updates a support ticket's status stage, enforcing valid lifecycle transitions and logging SLA timestamps.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Support ticket status updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid status transition or target ticket closed")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable final UUID id, @RequestParam final TicketStatus status) {
        final TicketResponse response = ticketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Support ticket status updated successfully"));
    }

    @Operation(
            summary = "Soft delete/close a support ticket",
            description = "Transitions a support ticket's status to CLOSED.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Support ticket closed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Support ticket not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable final UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Support ticket closed successfully"));
    }
}
