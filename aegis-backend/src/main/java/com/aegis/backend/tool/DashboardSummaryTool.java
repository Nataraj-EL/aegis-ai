package com.aegis.backend.tool;

import com.aegis.backend.repository.AuditEventRepository;
import com.aegis.backend.repository.DocumentChunkRepository;
import com.aegis.backend.repository.DocumentRepository;
import com.aegis.backend.repository.UserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DashboardSummaryTool implements Tool {

    private final UserRepository userRepository;
    private final AuditEventRepository auditEventRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public DashboardSummaryTool(
            final UserRepository userRepository,
            final AuditEventRepository auditEventRepository,
            final DocumentRepository documentRepository,
            final DocumentChunkRepository documentChunkRepository) {
        this.userRepository = userRepository;
        this.auditEventRepository = auditEventRepository;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Override
    public String getId() {
        return "dashboard_summary";
    }

    @Override
    public String getName() {
        return "Dashboard Summary Tool";
    }

    @Override
    public String getDescription() {
        return "Aggregates metrics for users, audit events, documents, and document chunks to generate an executive insights summary.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        final Map<String, Object> metrics = new HashMap<>();
        metrics.put("usersCount", userRepository.count());
        metrics.put("auditEventsCount", auditEventRepository.count());
        metrics.put("documentsCount", documentRepository.count());
        metrics.put("documentChunksCount", documentChunkRepository.count());
        return metrics;
    }
}
