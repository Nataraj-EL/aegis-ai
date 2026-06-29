package com.aegis.backend.repository;

import com.aegis.backend.dto.TicketMetricsProjection;
import com.aegis.backend.entity.Ticket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    @Query(
            "SELECT t.status AS status, t.priority AS priority, COUNT(t) AS count FROM Ticket t GROUP BY t.status, t.priority")
    List<TicketMetricsProjection> getTicketMetricsSummary();
}
