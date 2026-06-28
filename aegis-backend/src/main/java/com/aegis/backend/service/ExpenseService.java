package com.aegis.backend.service;

import com.aegis.backend.dto.ExpenseCreateRequest;
import com.aegis.backend.dto.ExpenseResponse;
import com.aegis.backend.entity.Expense;
import com.aegis.backend.entity.ExpenseCategory;
import com.aegis.backend.entity.ExpenseStatus;
import com.aegis.backend.repository.ExpenseRepository;
import com.aegis.backend.repository.ExpenseSpecifications;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(final ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(final ExpenseCreateRequest request, final String username) {
        final Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .status(ExpenseStatus.PENDING)
                .username(username)
                .build();

        final Expense saved = expenseRepository.save(expense);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(
            final String username,
            final ExpenseCategory category,
            final ExpenseStatus status,
            final LocalDateTime fromDate,
            final LocalDateTime toDate) {

        Specification<Expense> spec = ExpenseSpecifications.withUsername(username);

        if (category != null) {
            spec = spec.and(ExpenseSpecifications.withCategory(category));
        }
        if (status != null) {
            spec = spec.and(ExpenseSpecifications.withStatus(status));
        }
        if (fromDate != null) {
            spec = spec.and(ExpenseSpecifications.createdAfter(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(ExpenseSpecifications.createdBefore(toDate));
        }

        return expenseRepository.findAll(spec).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ExpenseResponse mapToResponse(final Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .status(expense.getStatus())
                .username(expense.getUsername())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
