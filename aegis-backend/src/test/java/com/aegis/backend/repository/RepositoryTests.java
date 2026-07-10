package com.aegis.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.aegis.backend.BaseTestContainer;
import com.aegis.backend.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class RepositoryTests extends BaseTestContainer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ProcurementRepository procurementRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private KnowledgeDocumentRepository knowledgeDocumentRepository;

    @Autowired
    private DashboardSnapshotRepository dashboardSnapshotRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    public void testUserAndRefreshTokenRepository() {
        final User user = User.builder()
                .username("test_user_repo")
                .email("test_user_repo@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();

        final User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        final Optional<User> fetchedUser = userRepository.findByUsername("test_user_repo");
        assertTrue(fetchedUser.isPresent());
        assertEquals("test_user_repo@example.com", fetchedUser.get().getEmail());

        final RefreshToken token = RefreshToken.builder()
                .user(savedUser)
                .token("hashed_refresh_token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        final RefreshToken savedToken = refreshTokenRepository.save(token);
        assertNotNull(savedToken.getId());

        final Optional<RefreshToken> fetchedToken = refreshTokenRepository.findByToken("hashed_refresh_token");
        assertTrue(fetchedToken.isPresent());
        assertEquals(savedUser.getId(), fetchedToken.get().getUser().getId());
    }

    @Test
    public void testCustomerAndDealRepository() {
        final Customer customer = Customer.builder()
                .name("Test Customer")
                .contactEmail("cust@test.com")
                .industry("Tech")
                .status(CustomerStatus.ACTIVE)
                .totalRevenue(new BigDecimal("50000.00"))
                .build();

        final Customer savedCustomer = customerRepository.save(customer);
        assertNotNull(savedCustomer.getId());

        final Deal deal = Deal.builder()
                .title("Big Deal")
                .amount(new BigDecimal("15000.00"))
                .status(DealStatus.OPEN)
                .customer(savedCustomer)
                .username("admin")
                .build();

        final Deal savedDeal = dealRepository.save(deal);
        assertNotNull(savedDeal.getId());
        assertEquals(savedCustomer.getId(), savedDeal.getCustomer().getId());
    }

    @Test
    public void testVendorAndProcurementRepository() {
        final Vendor vendor = Vendor.builder()
                .name("Test Vendor")
                .contactEmail("vendor@test.com")
                .category("IT")
                .status(VendorStatus.ACTIVE)
                .build();

        final Vendor savedVendor = vendorRepository.save(vendor);
        assertNotNull(savedVendor.getId());

        final ProcurementRequest request = ProcurementRequest.builder()
                .itemName("Laptops")
                .quantity(10)
                .estimatedCost(new BigDecimal("12000.00"))
                .status(ProcurementStatus.PENDING)
                .vendor(savedVendor)
                .build();

        final ProcurementRequest savedRequest = procurementRepository.save(request);
        assertNotNull(savedRequest.getId());
        assertEquals(savedVendor.getId(), savedRequest.getVendor().getId());
    }

    @Test
    public void testExpenseRepository() {
        final Expense expense = Expense.builder()
                .description("Cloud Hosting")
                .amount(new BigDecimal("450.00"))
                .category(ExpenseCategory.SOFTWARE)
                .status(ExpenseStatus.PENDING)
                .build();

        final Expense savedExpense = expenseRepository.save(expense);
        assertNotNull(savedExpense.getId());
    }

    @Test
    public void testInventoryItemRepository() {
        final InventoryItem item = InventoryItem.builder()
                .sku("SKU-100")
                .name("Developer Machine")
                .quantity(5)
                .unitPrice(new BigDecimal("1500.00"))
                .status(InventoryStatus.IN_STOCK)
                .build();

        final InventoryItem savedItem = inventoryItemRepository.save(item);
        assertNotNull(savedItem.getId());
    }

    @Test
    public void testInvoiceAndTicketRepository() {
        final Customer customer = customerRepository.save(Customer.builder()
                .name("Billing Client")
                .contactEmail("client@test.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        final Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-2026-001")
                .amount(new BigDecimal("2500.00"))
                .dueDate(LocalDateTime.now().plusDays(30))
                .status(InvoiceStatus.DRAFT)
                .customer(customer)
                .build();

        final Invoice savedInvoice = invoiceRepository.save(invoice);
        assertNotNull(savedInvoice.getId());

        final Ticket ticket = Ticket.builder()
                .ticketNumber("TKT-001")
                .title("Billing issue")
                .description("Cannot pay online")
                .status(TicketStatus.NEW)
                .priority(TicketPriority.HIGH)
                .customer(customer)
                .build();

        final Ticket savedTicket = ticketRepository.save(ticket);
        assertNotNull(savedTicket.getId());
    }

    @Test
    public void testKnowledgeAndSnapshotRepository() {
        final KnowledgeDocument doc = KnowledgeDocument.builder()
                .title("Docker Guidelines")
                .content("Layered JRE builds rules")
                .version(1)
                .status(KnowledgeStatus.DRAFT)
                .build();

        final KnowledgeDocument savedDoc = knowledgeDocumentRepository.save(doc);
        assertNotNull(savedDoc.getId());

        final DashboardSnapshot snapshot = DashboardSnapshot.builder()
                .title("Monthly Performance")
                .summaryData("{\"totalSales\": 50000}")
                .build();

        final DashboardSnapshot savedSnapshot = dashboardSnapshotRepository.save(snapshot);
        assertNotNull(savedSnapshot.getId());
    }

    @Test
    public void testApprovalAndAuditEventRepository() {
        final ApprovalRequest request = ApprovalRequest.builder()
                .entityType(ApprovalType.EXPENSE)
                .entityId(java.util.UUID.randomUUID())
                .requester("test_user")
                .status(ApprovalStatus.PENDING)
                .build();

        final ApprovalRequest savedRequest = approvalRepository.save(request);
        assertNotNull(savedRequest.getId());

        final AuditEvent event = AuditEvent.builder()
                .requestId("REQ-12345")
                .username("test_user")
                .action("USER_LOGIN")
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .build();

        final AuditEvent savedEvent = auditEventRepository.save(event);
        assertNotNull(savedEvent.getId());
    }
}
