package com.aegis.backend.service;

import com.aegis.backend.entity.Customer;
import com.aegis.backend.entity.Deal;
import com.aegis.backend.entity.DealStatus;
import com.aegis.backend.event.DealStatusChangedEvent;
import com.aegis.backend.repository.CustomerRepository;
import com.aegis.backend.repository.DealRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerRevenueUpdateListener {

    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;

    public CustomerRevenueUpdateListener(
            final CustomerRepository customerRepository, final DealRepository dealRepository) {
        this.customerRepository = customerRepository;
        this.dealRepository = dealRepository;
    }

    @EventListener
    public void onDealStatusChanged(final DealStatusChangedEvent event) {
        log.info("Processing Customer revenue update for Customer ID: {}", event.getCustomerId());

        final Customer customer =
                customerRepository.findById(event.getCustomerId()).orElse(null);
        if (customer == null) {
            log.error("Customer not found for ID: {}", event.getCustomerId());
            return;
        }

        final List<Deal> wonDeals = dealRepository.findByCustomerIdAndStatus(customer.getId(), DealStatus.CLOSED_WON);

        BigDecimal revenueSum = BigDecimal.ZERO;
        for (final Deal deal : wonDeals) {
            revenueSum = revenueSum.add(deal.getAmount());
        }

        customer.setTotalRevenue(revenueSum);
        customerRepository.save(customer);
        log.info("Customer ID: {} totalRevenue updated to: {}", customer.getId(), customer.getTotalRevenue());
    }
}
