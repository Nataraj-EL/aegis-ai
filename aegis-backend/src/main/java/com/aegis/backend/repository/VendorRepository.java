package com.aegis.backend.repository;

import com.aegis.backend.dto.VendorSummaryProjection;
import com.aegis.backend.entity.Vendor;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID>, JpaSpecificationExecutor<Vendor> {
    Optional<Vendor> findByName(String name);

    @Query("SELECT COUNT(v) AS count, COALESCE(AVG(v.rating), 0.0) AS averageRating FROM Vendor v")
    VendorSummaryProjection getVendorSummary();
}
