package com.aegis.backend.repository;

import com.aegis.backend.dto.ExpenseSummaryProjection;
import com.aegis.backend.entity.Expense;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    @Query("SELECT COUNT(e) AS count, COALESCE(SUM(e.amount), 0) AS totalAmount FROM Expense e")
    ExpenseSummaryProjection getExpenseSummary();
}
