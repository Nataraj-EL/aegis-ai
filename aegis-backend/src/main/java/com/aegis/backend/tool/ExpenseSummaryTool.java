package com.aegis.backend.tool;

import com.aegis.backend.entity.Expense;
import com.aegis.backend.repository.ExpenseRepository;
import com.aegis.backend.repository.ExpenseSpecifications;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ExpenseSummaryTool implements Tool {

    private final ExpenseRepository expenseRepository;

    public ExpenseSummaryTool(final ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public String getId() {
        return "expense_summary";
    }

    @Override
    public String getName() {
        return "Expense Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates totals oflogged expenses by category and lifecycle status. Accepts an optional 'username' parameter to filter results.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        final List<Expense> expenses;
        if (arguments != null && arguments.get("username") != null) {
            final String username = (String) arguments.get("username");
            expenses = expenseRepository.findAll(ExpenseSpecifications.withUsername(username));
        } else {
            expenses = expenseRepository.findAll();
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        final Map<String, BigDecimal> categoryTotals = new HashMap<>();
        final Map<String, BigDecimal> statusTotals = new HashMap<>();
        final List<Map<String, Object>> rawList = new ArrayList<>();

        for (final Expense expense : expenses) {
            totalAmount = totalAmount.add(expense.getAmount());

            final String categoryKey = expense.getCategory().name();
            categoryTotals.put(
                    categoryKey,
                    categoryTotals.getOrDefault(categoryKey, BigDecimal.ZERO).add(expense.getAmount()));

            final String statusKey = expense.getStatus().name();
            statusTotals.put(
                    statusKey,
                    statusTotals.getOrDefault(statusKey, BigDecimal.ZERO).add(expense.getAmount()));

            final Map<String, Object> item = new HashMap<>();
            item.put("id", expense.getId().toString());
            item.put("description", expense.getDescription());
            item.put("amount", expense.getAmount());
            item.put("category", categoryKey);
            item.put("status", statusKey);
            item.put("username", expense.getUsername());
            item.put(
                    "createdAt",
                    expense.getCreatedAt() != null ? expense.getCreatedAt().toString() : "");
            rawList.add(item);
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("categoryTotals", categoryTotals);
        result.put("statusTotals", statusTotals);
        result.put("rawExpenses", rawList);
        return result;
    }
}
