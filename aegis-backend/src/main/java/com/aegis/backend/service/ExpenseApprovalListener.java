package com.aegis.backend.service;

import com.aegis.backend.entity.ApprovalStatus;
import com.aegis.backend.entity.ApprovalType;
import com.aegis.backend.entity.Expense;
import com.aegis.backend.entity.ExpenseStatus;
import com.aegis.backend.event.ApprovalStatusChangedEvent;
import com.aegis.backend.repository.ExpenseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExpenseApprovalListener {

    private final ExpenseRepository expenseRepository;

    public ExpenseApprovalListener(final ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @EventListener
    public void onApprovalStatusChanged(final ApprovalStatusChangedEvent event) {
        if (event.getEntityType() == ApprovalType.EXPENSE) {
            log.info("Processing expense approval update event for Expense ID: {}", event.getEntityId());
            final Expense expense =
                    expenseRepository.findById(event.getEntityId()).orElse(null);
            if (expense == null) {
                log.error("Expense not found for ID: {}", event.getEntityId());
                return;
            }

            if (event.getStatus() == ApprovalStatus.APPROVED) {
                expense.setStatus(ExpenseStatus.APPROVED);
            } else if (event.getStatus() == ApprovalStatus.REJECTED) {
                expense.setStatus(ExpenseStatus.REJECTED);
            }
            expenseRepository.save(expense);
            log.info("Expense status updated to: {} for ID: {}", expense.getStatus(), expense.getId());
        }
    }
}
