package com.aegis.backend.service;

import com.aegis.backend.dto.ProcurementCreateRequest;
import com.aegis.backend.dto.ProcurementResponse;
import com.aegis.backend.entity.ProcurementRequest;
import com.aegis.backend.entity.ProcurementStatus;
import com.aegis.backend.entity.Vendor;
import com.aegis.backend.event.ProcurementCreatedEvent;
import com.aegis.backend.repository.ProcurementRepository;
import com.aegis.backend.repository.ProcurementSpecifications;
import com.aegis.backend.repository.VendorRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcurementService {

    private final ProcurementRepository procurementRepository;
    private final VendorRepository vendorRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProcurementService(
            final ProcurementRepository procurementRepository,
            final VendorRepository vendorRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.procurementRepository = procurementRepository;
        this.vendorRepository = vendorRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ProcurementResponse createProcurement(final ProcurementCreateRequest request, final String username) {
        Vendor vendor = null;
        if (request.getVendorId() != null) {
            vendor = vendorRepository
                    .findById(request.getVendorId())
                    .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        }

        final ProcurementRequest procurement = ProcurementRequest.builder()
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .estimatedCost(request.getEstimatedCost())
                .justification(request.getJustification())
                .status(ProcurementStatus.PENDING)
                .username(username)
                .vendor(vendor)
                .build();

        final ProcurementRequest saved = procurementRepository.save(procurement);

        // Publish decoupled ProcurementCreatedEvent
        eventPublisher.publishEvent(
                new ProcurementCreatedEvent(this, saved.getId(), saved.getUsername(), request.getApprover()));

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProcurementResponse> getProcurements(
            final String username, final ProcurementStatus status, final BigDecimal minCost, final BigDecimal maxCost) {

        Specification<ProcurementRequest> spec = ProcurementSpecifications.withUsername(username);

        if (status != null) {
            spec = spec.and(ProcurementSpecifications.withStatus(status));
        }
        if (minCost != null) {
            spec = spec.and(ProcurementSpecifications.costGreaterThanOrEqualTo(minCost));
        }
        if (maxCost != null) {
            spec = spec.and(ProcurementSpecifications.costLessThanOrEqualTo(maxCost));
        }

        return procurementRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProcurementResponse mapToResponse(final ProcurementRequest request) {
        return ProcurementResponse.builder()
                .id(request.getId())
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .estimatedCost(request.getEstimatedCost())
                .justification(request.getJustification())
                .status(request.getStatus())
                .username(request.getUsername())
                .createdAt(request.getCreatedAt())
                .vendorId(request.getVendor() != null ? request.getVendor().getId() : null)
                .vendorName(request.getVendor() != null ? request.getVendor().getName() : null)
                .build();
    }
}
