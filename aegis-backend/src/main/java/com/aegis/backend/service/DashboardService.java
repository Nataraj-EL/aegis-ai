package com.aegis.backend.service;

import com.aegis.backend.dto.DashboardSnapshotCreateRequest;
import com.aegis.backend.dto.DashboardSnapshotResponse;
import com.aegis.backend.dto.DashboardSummaryResponse;
import com.aegis.backend.dto.DealSummaryProjection;
import com.aegis.backend.dto.ExpenseSummaryProjection;
import com.aegis.backend.dto.InventorySummaryProjection;
import com.aegis.backend.dto.InvoiceSummaryProjection;
import com.aegis.backend.dto.ProcurementSummaryProjection;
import com.aegis.backend.dto.VendorSummaryProjection;
import com.aegis.backend.entity.DashboardSnapshot;
import com.aegis.backend.repository.CustomerRepository;
import com.aegis.backend.repository.DashboardSnapshotRepository;
import com.aegis.backend.repository.DashboardSnapshotSpecifications;
import com.aegis.backend.repository.DealRepository;
import com.aegis.backend.repository.ExpenseRepository;
import com.aegis.backend.repository.InventoryItemRepository;
import com.aegis.backend.repository.InvoiceRepository;
import com.aegis.backend.repository.KnowledgeDocumentRepository;
import com.aegis.backend.repository.ProcurementRepository;
import com.aegis.backend.repository.TicketRepository;
import com.aegis.backend.repository.VendorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final ProcurementRepository procurementRepository;
    private final VendorRepository vendorRepository;
    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final TicketRepository ticketRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final DashboardSnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    public DashboardService(
            final ExpenseRepository expenseRepository,
            final ProcurementRepository procurementRepository,
            final VendorRepository vendorRepository,
            final CustomerRepository customerRepository,
            final DealRepository dealRepository,
            final InventoryItemRepository inventoryItemRepository,
            final InvoiceRepository invoiceRepository,
            final TicketRepository ticketRepository,
            final KnowledgeDocumentRepository knowledgeDocumentRepository,
            final DashboardSnapshotRepository snapshotRepository,
            final ObjectMapper objectMapper) {
        this.expenseRepository = expenseRepository;
        this.procurementRepository = procurementRepository;
        this.vendorRepository = vendorRepository;
        this.customerRepository = customerRepository;
        this.dealRepository = dealRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.ticketRepository = ticketRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        log.info(
                "Computing real-time executive dashboard summary metrics directly from database aggregate projections.");

        final ExpenseSummaryProjection expenseProj = expenseRepository.getExpenseSummary();
        final ProcurementSummaryProjection procurementProj = procurementRepository.getProcurementSummary();
        final VendorSummaryProjection vendorProj = vendorRepository.getVendorSummary();
        final long customerCount = customerRepository.count();
        final DealSummaryProjection dealProj = dealRepository.getDealSummary();
        final InventorySummaryProjection inventoryProj = inventoryItemRepository.getInventorySummary();
        final InvoiceSummaryProjection invoiceProj = invoiceRepository.getInvoiceSummary();
        final long ticketCount = ticketRepository.count();
        final long knowledgeCount = knowledgeDocumentRepository.count();

        return DashboardSummaryResponse.builder()
                .expenseCount(expenseProj != null ? expenseProj.getCount() : 0L)
                .expenseTotalAmount(
                        expenseProj != null && expenseProj.getTotalAmount() != null
                                ? expenseProj.getTotalAmount()
                                : BigDecimal.ZERO)
                .procurementCount(procurementProj != null ? procurementProj.getCount() : 0L)
                .procurementTotalCost(
                        procurementProj != null && procurementProj.getTotalCost() != null
                                ? procurementProj.getTotalCost()
                                : BigDecimal.ZERO)
                .procurementPendingCost(
                        procurementProj != null && procurementProj.getPendingCost() != null
                                ? procurementProj.getPendingCost()
                                : BigDecimal.ZERO)
                .vendorCount(vendorProj != null ? vendorProj.getCount() : 0L)
                .averageVendorRating(vendorProj != null ? vendorProj.getAverageRating() : 0.0)
                .customerCount(customerCount)
                .dealCount(dealProj != null ? dealProj.getCount() : 0L)
                .dealTotalValue(
                        dealProj != null && dealProj.getTotalValue() != null
                                ? dealProj.getTotalValue()
                                : BigDecimal.ZERO)
                .dealClosedWonValue(
                        dealProj != null && dealProj.getClosedWonValue() != null
                                ? dealProj.getClosedWonValue()
                                : BigDecimal.ZERO)
                .inventoryItemCount(inventoryProj != null ? inventoryProj.getCount() : 0L)
                .inventoryTotalValuation(
                        inventoryProj != null && inventoryProj.getTotalValuation() != null
                                ? inventoryProj.getTotalValuation()
                                : BigDecimal.ZERO)
                .invoiceCount(invoiceProj != null ? invoiceProj.getCount() : 0L)
                .invoiceTotalAmount(
                        invoiceProj != null && invoiceProj.getTotalAmount() != null
                                ? invoiceProj.getTotalAmount()
                                : BigDecimal.ZERO)
                .invoicePaidAmount(
                        invoiceProj != null && invoiceProj.getPaidAmount() != null
                                ? invoiceProj.getPaidAmount()
                                : BigDecimal.ZERO)
                .invoiceOutstandingAmount(
                        invoiceProj != null && invoiceProj.getOutstandingAmount() != null
                                ? invoiceProj.getOutstandingAmount()
                                : BigDecimal.ZERO)
                .ticketCount(ticketCount)
                .knowledgeDocCount(knowledgeCount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public DashboardSnapshotResponse createSnapshot(final DashboardSnapshotCreateRequest request) {
        final DashboardSummaryResponse summary = getDashboardSummary();
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(summary);
        } catch (final Exception exception) {
            log.error("Failed to serialize dashboard summary to JSON payload for snapshot", exception);
            throw new IllegalStateException(
                    "Failed to serialize metrics summary: " + exception.getMessage(), exception);
        }

        final DashboardSnapshot snapshot = DashboardSnapshot.builder()
                .title(request.getTitle())
                .summaryData(jsonPayload)
                .build();

        final DashboardSnapshot saved = snapshotRepository.save(snapshot);
        log.info("Saved new corporate dashboard snapshot: '{}'", saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DashboardSnapshotResponse> getSnapshots(
            final String title, final LocalDateTime after, final LocalDateTime before) {
        Specification<DashboardSnapshot> spec = Specification.where(null);

        if (title != null) {
            spec = spec.and(DashboardSnapshotSpecifications.withTitleLike(title));
        }
        if (after != null) {
            spec = spec.and(DashboardSnapshotSpecifications.createdAfter(after));
        }
        if (before != null) {
            spec = spec.and(DashboardSnapshotSpecifications.createdBefore(before));
        }

        return snapshotRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardSnapshotResponse getSnapshotById(final UUID id) {
        final DashboardSnapshot snapshot = snapshotRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dashboard snapshot not found"));
        return mapToResponse(snapshot);
    }

    @Transactional
    public void deleteSnapshot(final UUID id) {
        final DashboardSnapshot snapshot = snapshotRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dashboard snapshot not found"));
        snapshotRepository.delete(snapshot);
        log.info("Hard deleted dashboard snapshot ID: {}", id);
    }

    private DashboardSnapshotResponse mapToResponse(final DashboardSnapshot snapshot) {
        DashboardSummaryResponse summary;
        try {
            summary = objectMapper.readValue(snapshot.getSummaryData(), DashboardSummaryResponse.class);
        } catch (final Exception exception) {
            log.error(
                    "Failed to deserialize snapshot summary payload JSON for snapshot ID: {}",
                    snapshot.getId(),
                    exception);
            summary = DashboardSummaryResponse.builder()
                    .createdAt(snapshot.getCreatedAt())
                    .build();
        }

        return DashboardSnapshotResponse.builder()
                .id(snapshot.getId())
                .title(snapshot.getTitle())
                .summary(summary)
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}
