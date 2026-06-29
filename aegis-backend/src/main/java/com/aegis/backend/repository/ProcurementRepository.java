package com.aegis.backend.repository;

import com.aegis.backend.dto.ProcurementSummaryProjection;
import com.aegis.backend.entity.ProcurementRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcurementRepository
        extends JpaRepository<ProcurementRequest, UUID>, JpaSpecificationExecutor<ProcurementRequest> {
    boolean existsByVendorId(UUID vendorId);

    @Query("SELECT COUNT(p) AS count, "
            + "COALESCE(SUM(p.estimatedCost), 0) AS totalCost, "
            + "COALESCE(SUM(CASE WHEN p.status = 'PENDING' THEN p.estimatedCost ELSE 0 END), 0) AS pendingCost "
            + "FROM ProcurementRequest p")
    ProcurementSummaryProjection getProcurementSummary();
}
