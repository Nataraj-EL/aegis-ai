package com.aegis.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LoginFailureEvent extends ApplicationEvent {
    private final String username;
    private final String reason;

    public LoginFailureEvent(final Object source, final String username, final String reason) {
        super(source);
        this.username = username;
        this.reason = reason;
    }
}
