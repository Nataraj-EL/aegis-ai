package com.aegis.backend.repository;

import com.aegis.backend.entity.Deal;
import com.aegis.backend.entity.DealStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID>, JpaSpecificationExecutor<Deal> {
    List<Deal> findByCustomerIdAndStatus(UUID customerId, DealStatus status);
}
