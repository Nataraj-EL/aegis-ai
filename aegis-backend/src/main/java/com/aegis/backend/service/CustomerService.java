package com.aegis.backend.service;

import com.aegis.backend.dto.CustomerCreateRequest;
import com.aegis.backend.dto.CustomerResponse;
import com.aegis.backend.entity.Customer;
import com.aegis.backend.entity.CustomerStatus;
import com.aegis.backend.event.CustomerCreatedEvent;
import com.aegis.backend.repository.CustomerRepository;
import com.aegis.backend.repository.CustomerSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CustomerService(
            final CustomerRepository customerRepository, final ApplicationEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CustomerResponse createCustomer(final CustomerCreateRequest request) {
        if (customerRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Customer with name '%s' already exists", request.getName()));
        }

        if (customerRepository.findByContactEmail(request.getContactEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Customer with email '%s' already exists", request.getContactEmail()));
        }

        final Customer customer = Customer.builder()
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .industry(request.getIndustry())
                .status(request.getStatus())
                .totalRevenue(request.getTotalRevenue())
                .build();

        final Customer saved = customerRepository.save(customer);

        // Publish extended CustomerCreatedEvent
        eventPublisher.publishEvent(new CustomerCreatedEvent(
                this, saved.getId(), saved.getName(), saved.getStatus(), saved.getCreatedAt()));

        return mapToResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(final UUID id, final CustomerCreateRequest request) {
        final Customer customer =
                customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        customerRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException(
                        String.format("Customer with name '%s' already exists", request.getName()));
            }
        });

        customerRepository.findByContactEmail(request.getContactEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException(
                        String.format("Customer with email '%s' already exists", request.getContactEmail()));
            }
        });

        customer.setName(request.getName());
        customer.setContactEmail(request.getContactEmail());
        customer.setIndustry(request.getIndustry());
        customer.setStatus(request.getStatus());
        customer.setTotalRevenue(request.getTotalRevenue());

        final Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCustomer(final UUID id) {
        final Customer customer =
                customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        customer.setStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers(
            final CustomerStatus status, final String industry, final BigDecimal minRevenue) {
        Specification<Customer> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(CustomerSpecifications.withStatus(status));
        }
        if (industry != null) {
            spec = spec.and(CustomerSpecifications.withIndustry(industry));
        }
        if (minRevenue != null) {
            spec = spec.and(CustomerSpecifications.revenueGreaterThanOrEqualTo(minRevenue));
        }

        return customerRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(final UUID id) {
        final Customer customer =
                customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return mapToResponse(customer);
    }

    private CustomerResponse mapToResponse(final Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .contactEmail(customer.getContactEmail())
                .industry(customer.getIndustry())
                .status(customer.getStatus())
                .totalRevenue(customer.getTotalRevenue())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
