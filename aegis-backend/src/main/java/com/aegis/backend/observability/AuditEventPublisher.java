package com.aegis.backend.observability;

import com.aegis.backend.entity.AuditEvent;
import com.aegis.backend.repository.AuditEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuditEventPublisher {

    private final AuditEventRepository auditEventRepository;

    public AuditEventPublisher(final AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishEvent(
            final String requestId,
            final String username,
            final String action,
            final String status,
            final Long executionTime,
            final String metadata) {
        log.info("Publishing audit event: Action='{}', Status='{}', RequestId='{}'", action, status, requestId);
        try {
            final AuditEvent event = AuditEvent.builder()
                    .requestId(requestId != null ? requestId : "N/A")
                    .username(username)
                    .action(action)
                    .status(status)
                    .executionTime(executionTime)
                    .metadata(metadata)
                    .build();

            auditEventRepository.save(event);
        } catch (final Exception exception) {
            log.error("Failed to save audit event to repository", exception);
        }
    }
}
