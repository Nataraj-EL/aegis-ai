package com.aegis.backend.service;

import com.aegis.backend.dto.InvoiceCreateRequest;
import com.aegis.backend.dto.InvoiceResponse;
import com.aegis.backend.entity.Customer;
import com.aegis.backend.entity.Deal;
import com.aegis.backend.entity.Invoice;
import com.aegis.backend.entity.InvoiceStatus;
import com.aegis.backend.event.InvoiceStatusChangedEvent;
import com.aegis.backend.repository.CustomerRepository;
import com.aegis.backend.repository.DealRepository;
import com.aegis.backend.repository.InvoiceRepository;
import com.aegis.backend.repository.InvoiceSpecifications;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InvoiceService(
            final InvoiceRepository invoiceRepository,
            final CustomerRepository customerRepository,
            final DealRepository dealRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.dealRepository = dealRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public InvoiceResponse createInvoice(final InvoiceCreateRequest request) {
        final Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Deal deal = null;
        if (request.getDealId() != null) {
            deal = dealRepository
                    .findById(request.getDealId())
                    .orElseThrow(() -> new IllegalArgumentException("Deal not found"));
            if (!deal.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException("Selected deal must belong to the selected customer");
            }
        }

        if (invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Invoice with invoice number '%s' already exists", request.getInvoiceNumber()));
        }

        final Invoice invoice = Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .amount(request.getAmount())
                .status(InvoiceStatus.DRAFT)
                .customer(customer)
                .deal(deal)
                .dueDate(request.getDueDate())
                .build();

        final Invoice saved = invoiceRepository.save(invoice);
        return mapToResponse(saved);
    }

    @Transactional
    public InvoiceResponse updateInvoiceStatus(final UUID id, final InvoiceStatus newStatus) {
        final Invoice invoice =
                invoiceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        final InvoiceStatus oldStatus = invoice.getStatus();

        if (oldStatus == InvoiceStatus.PAID || oldStatus == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transition from a terminal state: " + oldStatus);
        }

        // Validate transitions:
        // DRAFT -> PENDING, CANCELLED
        // PENDING -> PAID, OVERDUE, CANCELLED
        // OVERDUE -> PAID, CANCELLED
        boolean valid = false;
        if (oldStatus == InvoiceStatus.DRAFT) {
            valid = (newStatus == InvoiceStatus.PENDING || newStatus == InvoiceStatus.CANCELLED);
        } else if (oldStatus == InvoiceStatus.PENDING) {
            valid = (newStatus == InvoiceStatus.PAID
                    || newStatus == InvoiceStatus.OVERDUE
                    || newStatus == InvoiceStatus.CANCELLED);
        } else if (oldStatus == InvoiceStatus.OVERDUE) {
            valid = (newStatus == InvoiceStatus.PAID || newStatus == InvoiceStatus.CANCELLED);
        }

        if (!valid) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", oldStatus, newStatus));
        }

        invoice.setStatus(newStatus);
        final Invoice saved = invoiceRepository.save(invoice);

        if (newStatus != oldStatus) {
            eventPublisher.publishEvent(new InvoiceStatusChangedEvent(
                    this, saved.getId(), saved.getCustomer().getId(), oldStatus, newStatus));
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteInvoice(final UUID id) {
        // Marks status as CANCELLED (Soft delete)
        updateInvoiceStatus(id, InvoiceStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoices(
            final InvoiceStatus status,
            final UUID customerId,
            final LocalDateTime fromDate,
            final LocalDateTime toDate,
            final BigDecimal minAmount) {
        Specification<Invoice> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(InvoiceSpecifications.withStatus(status));
        }
        if (customerId != null) {
            spec = spec.and(InvoiceSpecifications.withCustomerId(customerId));
        }
        if (fromDate != null || toDate != null) {
            spec = spec.and(InvoiceSpecifications.dueDateBetween(fromDate, toDate));
        }
        if (minAmount != null) {
            spec = spec.and(InvoiceSpecifications.amountGreaterThanOrEqualTo(minAmount));
        }

        return invoiceRepository.findAll(spec).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(final UUID id) {
        final Invoice invoice =
                invoiceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return mapToResponse(invoice);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void checkOverdueInvoices() {
        log.info("Running scheduled check for overdue invoices...");
        final List<Invoice> overduePendingInvoices =
                invoiceRepository.findByStatusAndDueDateBefore(InvoiceStatus.PENDING, LocalDateTime.now());

        for (final Invoice invoice : overduePendingInvoices) {
            log.info("Transitioning invoice {} to OVERDUE status", invoice.getInvoiceNumber());
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
            eventPublisher.publishEvent(new InvoiceStatusChangedEvent(
                    this,
                    invoice.getId(),
                    invoice.getCustomer().getId(),
                    InvoiceStatus.PENDING,
                    InvoiceStatus.OVERDUE));
        }
    }

    private InvoiceResponse mapToResponse(final Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .customerId(invoice.getCustomer().getId())
                .customerName(invoice.getCustomer().getName())
                .dealId(invoice.getDeal() != null ? invoice.getDeal().getId() : null)
                .dealTitle(invoice.getDeal() != null ? invoice.getDeal().getTitle() : null)
                .dueDate(invoice.getDueDate())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
