package com.aegis.backend.repository;

import com.aegis.backend.dto.DealSummaryProjection;
import com.aegis.backend.entity.Deal;
import com.aegis.backend.entity.DealStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID>, JpaSpecificationExecutor<Deal> {
    List<Deal> findByCustomerIdAndStatus(UUID customerId, DealStatus status);

    @Query("SELECT COUNT(d) AS count, "
            + "COALESCE(SUM(d.amount), 0) AS totalValue, "
            + "COALESCE(SUM(CASE WHEN d.status = 'CLOSED_WON' THEN d.amount ELSE 0 END), 0) AS closedWonValue "
            + "FROM Deal d")
    DealSummaryProjection getDealSummary();
}
