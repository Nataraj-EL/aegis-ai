package com.aegis.backend.service;

import com.aegis.backend.repository.KnowledgeDocumentRepository;
import com.aegis.backend.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private static final String TAG_STATUS = "status";
    private static final String TAG_ACTION = "action";

    private final MeterRegistry registry;
    private final ObjectProvider<UserRepository> userRepositoryProvider;
    private final ObjectProvider<KnowledgeDocumentRepository> knowledgeDocumentRepositoryProvider;

    public MetricsService(
            final MeterRegistry registry,
            final ObjectProvider<UserRepository> userRepositoryProvider,
            final ObjectProvider<KnowledgeDocumentRepository> knowledgeDocumentRepositoryProvider) {
        this.registry = registry;
        this.userRepositoryProvider = userRepositoryProvider;
        this.knowledgeDocumentRepositoryProvider = knowledgeDocumentRepositoryProvider;
    }

    @PostConstruct
    public void initGauges() {
        registry.gauge("aegis.users.registered", this, service -> {
            final UserRepository repo = service.userRepositoryProvider.getIfAvailable();
            return repo != null ? repo.count() : 0L;
        });

        registry.gauge("aegis.knowledge.documents", this, service -> {
            final KnowledgeDocumentRepository repo = service.knowledgeDocumentRepositoryProvider.getIfAvailable();
            return repo != null ? repo.count() : 0L;
        });
    }

    public void recordAiRequest(final String model, final String status, final long durationMs) {
        registry.counter("aegis.ai.requests.total", "model", model, TAG_STATUS, status)
                .increment();
        registry.timer("aegis.ai.requests.latency", "model", model).record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordAiProviderRequest(final String provider, final String status, final long durationMs) {
        registry.counter("aegis.ai.provider.requests.total", "provider", provider, TAG_STATUS, status)
                .increment();
        registry.timer("aegis.ai.provider.requests.latency", "provider", provider)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordToolExecution(final String toolId, final String status, final long durationMs) {
        registry.counter("aegis.tool.executions.total", "tool", toolId, TAG_STATUS, status)
                .increment();
        registry.timer("aegis.tool.executions.latency", "tool", toolId).record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordAgentExecution(final String agentId, final String status, final long durationMs) {
        registry.counter("aegis.agent.executions.total", "agent", agentId, TAG_STATUS, status)
                .increment();
        registry.timer("aegis.agent.executions.latency", "agent", agentId).record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void incrementAuthEvent(final String action) {
        registry.counter("aegis.auth.events.total", TAG_ACTION, action).increment();
    }

    public void incrementApproval(final String action) {
        registry.counter("aegis.approvals.total", TAG_ACTION, action).increment();
    }

    public void incrementProcurement(final String action) {
        registry.counter("aegis.procurements.total", TAG_ACTION, action).increment();
    }

    public void incrementExpense(final String action) {
        registry.counter("aegis.expenses.total", TAG_ACTION, action).increment();
    }

    public void incrementCustomer(final String action) {
        registry.counter("aegis.customers.total", TAG_ACTION, action).increment();
    }

    public void incrementDeal(final String action) {
        registry.counter("aegis.deals.total", TAG_ACTION, action).increment();
    }

    public void incrementInventory(final String action) {
        registry.counter("aegis.inventory.total", TAG_ACTION, action).increment();
    }

    public void incrementInvoice(final String action) {
        registry.counter("aegis.invoices.total", TAG_ACTION, action).increment();
    }

    public void incrementTicket(final String action) {
        registry.counter("aegis.tickets.total", TAG_ACTION, action).increment();
    }

    public void incrementRagSearch(final String status) {
        registry.counter("aegis.rag.searches.total", TAG_STATUS, status).increment();
    }
}
