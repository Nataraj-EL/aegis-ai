package com.aegis.backend.repository;

import com.aegis.backend.entity.DashboardSnapshot;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardSnapshotRepository
        extends JpaRepository<DashboardSnapshot, UUID>, JpaSpecificationExecutor<DashboardSnapshot> {}
