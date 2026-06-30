package com.aegis.backend.observability;

import com.aegis.backend.service.MetricsService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BusinessMetricsAspect {

    private static final String ACTION_CREATE = "create";

    private final MetricsService metricsService;

    public BusinessMetricsAspect(final MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @AfterReturning("execution(* com.aegis.backend.service.ApprovalService.createApproval(..))")
    public void afterCreateApproval(final JoinPoint joinPoint) {
        metricsService.incrementApproval(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.ApprovalService.makeDecision(..))")
    public void afterMakeDecision(final JoinPoint joinPoint) {
        metricsService.incrementApproval("decision");
    }

    @AfterReturning("execution(* com.aegis.backend.service.ProcurementService.createProcurement(..))")
    public void afterCreateProcurement(final JoinPoint joinPoint) {
        metricsService.incrementProcurement(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.ExpenseService.createExpense(..))")
    public void afterCreateExpense(final JoinPoint joinPoint) {
        metricsService.incrementExpense(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.CustomerService.createCustomer(..))")
    public void afterCreateCustomer(final JoinPoint joinPoint) {
        metricsService.incrementCustomer(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.CustomerService.updateCustomer(..))")
    public void afterUpdateCustomer(final JoinPoint joinPoint) {
        metricsService.incrementCustomer("update");
    }

    @AfterReturning("execution(* com.aegis.backend.service.CustomerService.deleteCustomer(..))")
    public void afterDeleteCustomer(final JoinPoint joinPoint) {
        metricsService.incrementCustomer("delete");
    }

    @AfterReturning("execution(* com.aegis.backend.service.DealService.createDeal(..))")
    public void afterCreateDeal(final JoinPoint joinPoint) {
        metricsService.incrementDeal(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.DealService.updateDealStatus(..))")
    public void afterUpdateDealStatus(final JoinPoint joinPoint) {
        metricsService.incrementDeal("update_status");
    }

    @AfterReturning("execution(* com.aegis.backend.service.InventoryItemService.createInventoryItem(..))")
    public void afterCreateInventoryItem(final JoinPoint joinPoint) {
        metricsService.incrementInventory(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.InventoryItemService.updateQuantity(..))")
    public void afterUpdateQuantity(final JoinPoint joinPoint) {
        metricsService.incrementInventory("adjust_stock");
    }

    @AfterReturning("execution(* com.aegis.backend.service.InvoiceService.createInvoice(..))")
    public void afterCreateInvoice(final JoinPoint joinPoint) {
        metricsService.incrementInvoice(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.InvoiceService.updateInvoice(..))")
    public void afterUpdateInvoice(final JoinPoint joinPoint) {
        metricsService.incrementInvoice("update");
    }

    @AfterReturning("execution(* com.aegis.backend.service.InvoiceService.deleteInvoice(..))")
    public void afterDeleteInvoice(final JoinPoint joinPoint) {
        metricsService.incrementInvoice("delete");
    }

    @AfterReturning("execution(* com.aegis.backend.service.TicketService.createTicket(..))")
    public void afterCreateTicket(final JoinPoint joinPoint) {
        metricsService.incrementTicket(ACTION_CREATE);
    }

    @AfterReturning("execution(* com.aegis.backend.service.TicketService.updateTicket(..))")
    public void afterUpdateTicket(final JoinPoint joinPoint) {
        metricsService.incrementTicket("update");
    }

    @AfterReturning("execution(* com.aegis.backend.service.TicketService.deleteTicket(..))")
    public void afterDeleteTicket(final JoinPoint joinPoint) {
        metricsService.incrementTicket("delete");
    }
}
