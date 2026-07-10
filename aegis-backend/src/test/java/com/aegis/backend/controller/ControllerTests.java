package com.aegis.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aegis.backend.BaseTestContainer;
import com.aegis.backend.ai.AiProvider;
import com.aegis.backend.config.AiProperties;
import com.aegis.backend.dto.CustomerCreateRequest;
import com.aegis.backend.dto.CustomerResponse;
import com.aegis.backend.entity.CustomerStatus;
import com.aegis.backend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ControllerTests extends BaseTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private DealService dealService;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private ProcurementService procurementService;

    @MockBean
    private InventoryItemService inventoryItemService;

    @MockBean
    private VendorService vendorService;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private KnowledgeDocumentService knowledgeDocumentService;

    @MockBean
    private ApprovalService approvalService;

    @MockBean
    private com.aegis.backend.agent.OrchestratorAgent orchestratorAgent;

    @MockBean
    private List<AiProvider> aiProviders;

    @MockBean
    private AiProperties aiProperties;

    @Test
    public void testGetCustomersUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/customers")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetCustomersAuthenticatedAsUser() throws Exception {
        final CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .contactEmail("alice@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        Mockito.when(customerService.getCustomers(null, null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/customers")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCreateCustomerAsUserForbidden() throws Exception {
        final CustomerCreateRequest request = CustomerCreateRequest.builder()
                .name("New Client")
                .contactEmail("new@test.com")
                .industry("Tech")
                .status(CustomerStatus.ACTIVE)
                .totalRevenue(BigDecimal.ZERO)
                .build();

        mockMvc.perform(post("/api/v1/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testCreateCustomerAsManagerAllowed() throws Exception {
        final CustomerCreateRequest request = CustomerCreateRequest.builder()
                .name("New Client")
                .contactEmail("new@test.com")
                .industry("Tech")
                .status(CustomerStatus.ACTIVE)
                .totalRevenue(BigDecimal.ZERO)
                .build();

        final CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .name("New Client")
                .contactEmail("new@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        Mockito.when(customerService.createCustomer(Mockito.any(CustomerCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetProvidersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/ai/providers")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetExecutiveSummary() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetInvoices() throws Exception {
        mockMvc.perform(get("/api/v1/invoices")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetTickets() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")).andExpect(status().isOk());
    }
}
