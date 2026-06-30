package com.aegis.backend.event;

import com.aegis.backend.observability.AuditEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditEventListener {

    private final AuditEventPublisher auditEventPublisher;

    public AuthAuditEventListener(final AuditEventPublisher auditEventPublisher) {
        this.auditEventPublisher = auditEventPublisher;
    }

    @EventListener
    public void handleLoginSuccess(final LoginSuccessEvent event) {
        auditEventPublisher.publishEvent(
                "SYSTEM", event.getUsername(), "USER_LOGIN", "SUCCESS", 0L, "Authentication successful");
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
    }

    @EventListener
    public void handleLogout(final LogoutEvent event) {
        auditEventPublisher.publishEvent(
                "SYSTEM", event.getUsername(), "USER_LOGOUT", "SUCCESS", 0L, "User logged out successfully");
    }
}
