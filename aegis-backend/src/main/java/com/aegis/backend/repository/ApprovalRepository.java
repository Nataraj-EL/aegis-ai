package com.aegis.backend.repository;

import com.aegis.backend.entity.ApprovalRequest;
import com.aegis.backend.entity.ApprovalStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<ApprovalRequest, UUID> {
    List<ApprovalRequest> findByApproverAndStatus(String approver, ApprovalStatus status);

    List<ApprovalRequest> findByStatus(ApprovalStatus status);
}
