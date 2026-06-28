package com.aegis.backend.service;

import com.aegis.backend.dto.DealCreateRequest;
import com.aegis.backend.dto.DealResponse;
import com.aegis.backend.entity.Customer;
import com.aegis.backend.entity.Deal;
import com.aegis.backend.entity.DealStatus;
import com.aegis.backend.event.DealStatusChangedEvent;
import com.aegis.backend.repository.CustomerRepository;
import com.aegis.backend.repository.DealRepository;
import com.aegis.backend.repository.DealSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealService {

    private final DealRepository dealRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DealService(
            final DealRepository dealRepository,
            final CustomerRepository customerRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.dealRepository = dealRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DealResponse createDeal(final DealCreateRequest request, final String username) {
        final Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        final Deal deal = Deal.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .status(DealStatus.OPEN)
                .customer(customer)
                .username(username)
                .build();

        final Deal saved = dealRepository.save(deal);
        return mapToResponse(saved);
    }

    @Transactional
    public DealResponse updateDealStatus(final UUID id, final DealStatus newStatus) {
        final Deal deal = dealRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Deal not found"));

        if (newStatus == DealStatus.OPEN) {
            throw new IllegalStateException("Cannot change status to OPEN");
        }

        if (deal.getStatus() != DealStatus.OPEN) {
            throw new IllegalStateException("Cannot change status of a finalized deal");
        }

        final DealStatus oldStatus = deal.getStatus();
        deal.setStatus(newStatus);
        final Deal saved = dealRepository.save(deal);

        if (newStatus != oldStatus) {
            eventPublisher.publishEvent(new DealStatusChangedEvent(
                    this, saved.getId(), saved.getCustomer().getId(), newStatus));
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DealResponse> getDeals(final DealStatus status, final UUID customerId, final BigDecimal minAmount) {
        Specification<Deal> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(DealSpecifications.withStatus(status));
        }
        if (customerId != null) {
            spec = spec.and(DealSpecifications.withCustomerId(customerId));
        }
        if (minAmount != null) {
            spec = spec.and(DealSpecifications.amountGreaterThanOrEqualTo(minAmount));
        }

        return dealRepository.findAll(spec).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DealResponse getDeal(final UUID id) {
        final Deal deal = dealRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Deal not found"));
        return mapToResponse(deal);
    }

    private DealResponse mapToResponse(final Deal deal) {
        return DealResponse.builder()
                .id(deal.getId())
                .title(deal.getTitle())
                .amount(deal.getAmount())
                .status(deal.getStatus())
                .customerId(deal.getCustomer().getId())
                .customerName(deal.getCustomer().getName())
                .username(deal.getUsername())
                .createdAt(deal.getCreatedAt())
                .build();
    }
}
