package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.ExpenseCreateRequest;
import com.aegis.backend.dto.ExpenseResponse;
import com.aegis.backend.entity.ExpenseCategory;
import com.aegis.backend.entity.ExpenseStatus;
import com.aegis.backend.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Expense Controller", description = "REST endpoints to log and filter business expense entries")
@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(final ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Operation(
            summary = "Submit a new business expense",
            description = "Logs a new expense request under PENDING status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Expense successfully logged"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody final ExpenseCreateRequest request) {
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final ExpenseResponse response = expenseService.createExpense(request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Expense registered successfully"));
    }

    @Operation(
            summary = "List and filter user expenses",
            description =
                    "Retrieves expenses filed by the logged-in user with optional category, status, and creation date range filters.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Expenses retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpenses(
            @RequestParam(required = false) final ExpenseCategory category,
            @RequestParam(required = false) final ExpenseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    final LocalDateTime to) {
        final String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        final List<ExpenseResponse> response = expenseService.getExpenses(username, category, status, from, to);
        return ResponseEntity.ok(ApiResponse.success(response, "Expenses loaded successfully"));
    }
}
