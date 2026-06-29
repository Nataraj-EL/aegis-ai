package com.aegis.backend.service;

import com.aegis.backend.dto.TicketCreateRequest;
import com.aegis.backend.dto.TicketResponse;
import com.aegis.backend.entity.Customer;
import com.aegis.backend.entity.Ticket;
import com.aegis.backend.entity.TicketPriority;
import com.aegis.backend.entity.TicketStatus;
import com.aegis.backend.event.TicketStatusChangedEvent;
import com.aegis.backend.repository.CustomerRepository;
import com.aegis.backend.repository.TicketRepository;
import com.aegis.backend.repository.TicketSpecifications;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TicketService(
            final TicketRepository ticketRepository,
            final CustomerRepository customerRepository,
            final ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TicketResponse createTicket(final TicketCreateRequest request) {
        final Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Generate timestamp-based ticket number format: TKT-20260629-XXXX
        final String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ticketNumber;
        int attempts = 0;
        do {
            final int suffix = ThreadLocalRandom.current().nextInt(1_000, 10_000);
            ticketNumber = String.format("TKT-%s-%04d", datePart, suffix);
            attempts++;
        } while (ticketRepository.findByTicketNumber(ticketNumber).isPresent() && attempts < 10);

        final Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TicketStatus.NEW)
                .priority(request.getPriority())
                .customer(customer)
                .assignee(request.getAssignee())
                .build();

        final Ticket saved = ticketRepository.save(ticket);
        log.info("Registered new support ticket: {}", saved.getTicketNumber());
        return mapToResponse(saved);
    }

    @Transactional
    public TicketResponse updateTicketStatus(final UUID id, final TicketStatus newStatus) {
        final Ticket ticket =
                ticketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        final TicketStatus oldStatus = ticket.getStatus();

        if (oldStatus == TicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot transition from terminal state: " + oldStatus);
        }

        // Validate transitions:
        // NEW -> ASSIGNED, CLOSED
        // ASSIGNED -> IN_PROGRESS, RESOLVED, CLOSED
        // IN_PROGRESS -> RESOLVED, CLOSED
        // RESOLVED -> IN_PROGRESS, CLOSED
        boolean valid = false;
        if (oldStatus == TicketStatus.NEW) {
            valid = (newStatus == TicketStatus.ASSIGNED || newStatus == TicketStatus.CLOSED);
        } else if (oldStatus == TicketStatus.ASSIGNED) {
            valid = (newStatus == TicketStatus.IN_PROGRESS
                    || newStatus == TicketStatus.RESOLVED
                    || newStatus == TicketStatus.CLOSED);
        } else if (oldStatus == TicketStatus.IN_PROGRESS) {
            valid = (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED);
        } else if (oldStatus == TicketStatus.RESOLVED) {
            valid = (newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.CLOSED);
        }

        if (!valid) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", oldStatus, newStatus));
        }

        ticket.setStatus(newStatus);

        // Auto-assign timestamp triggers
        if (newStatus == TicketStatus.ASSIGNED) {
            ticket.setAssignedAt(LocalDateTime.now());
        } else if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        final Ticket saved = ticketRepository.save(ticket);

        if (newStatus != oldStatus) {
            eventPublisher.publishEvent(new TicketStatusChangedEvent(
                    this, saved.getId(), saved.getCustomer().getId(), oldStatus, newStatus));
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteTicket(final UUID id) {
        // Marks ticket as CLOSED (Soft delete)
        updateTicketStatus(id, TicketStatus.CLOSED);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTickets(
            final TicketStatus status,
            final TicketPriority priority,
            final UUID customerId,
            final String assignee,
            final LocalDateTime fromDate,
            final LocalDateTime toDate) {
        Specification<Ticket> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(TicketSpecifications.withStatus(status));
        }
        if (priority != null) {
            spec = spec.and(TicketSpecifications.withPriority(priority));
        }
        if (customerId != null) {
            spec = spec.and(TicketSpecifications.withCustomerId(customerId));
        }
        if (assignee != null) {
            spec = spec.and(TicketSpecifications.withAssignee(assignee));
        }
        if (fromDate != null || toDate != null) {
            spec = spec.and(TicketSpecifications.createdAtBetween(fromDate, toDate));
        }

        return ticketRepository.findAll(spec).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(final UUID id) {
        final Ticket ticket =
                ticketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        return mapToResponse(ticket);
    }

    private TicketResponse mapToResponse(final Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .customerId(ticket.getCustomer().getId())
                .customerName(ticket.getCustomer().getName())
                .assignee(ticket.getAssignee())
                .assignedAt(ticket.getAssignedAt())
                .resolvedAt(ticket.getResolvedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
