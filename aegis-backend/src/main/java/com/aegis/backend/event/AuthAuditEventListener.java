package com.aegis.backend.event;

import com.aegis.backend.observability.AuditEventPublisher;
import com.aegis.backend.service.MetricsService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditEventListener {

    private final AuditEventPublisher auditEventPublisher;
    private final MetricsService metricsService;

    public AuthAuditEventListener(final AuditEventPublisher auditEventPublisher, final MetricsService metricsService) {
        this.auditEventPublisher = auditEventPublisher;
        this.metricsService = metricsService;
    }

    @EventListener
    public void handleLoginSuccess(final LoginSuccessEvent event) {
        auditEventPublisher.publishEvent(
                "SYSTEM", event.getUsername(), "USER_LOGIN", "SUCCESS", 0L, "Authentication successful");
        metricsService.incrementAuthEvent("login");
    }

    @EventListener
    public void handleLoginFailure(final LoginFailureEvent event) {
        auditEventPublisher.publishEvent(
                "SYSTEM",
                event.getUsername(),
                "USER_LOGIN",
                "FAILURE",
                0L,
                "Authentication failed: " + event.getReason());
        metricsService.incrementAuthEvent("failure");
    }

    @EventListener
    public void handleLogout(final LogoutEvent event) {
        auditEventPublisher.publishEvent(
                "SYSTEM", event.getUsername(), "USER_LOGOUT", "SUCCESS", 0L, "User logged out successfully");
        metricsService.incrementAuthEvent("logout");
    }
}
