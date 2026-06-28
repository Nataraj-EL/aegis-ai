package com.aegis.backend.repository;

import com.aegis.backend.entity.ProcurementRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcurementRepository
        extends JpaRepository<ProcurementRequest, UUID>, JpaSpecificationExecutor<ProcurementRequest> {}
