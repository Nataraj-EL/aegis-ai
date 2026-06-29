package com.aegis.backend.repository;

import com.aegis.backend.dto.InvoiceMetricsProjection;
import com.aegis.backend.entity.Invoice;
import com.aegis.backend.entity.InvoiceStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDateTime dateTime);

    @Query(
            "SELECT i.status AS status, COUNT(i) AS count, SUM(i.amount) AS totalAmount FROM Invoice i GROUP BY i.status")
    List<InvoiceMetricsProjection> getInvoiceMetricsSummary();
}
