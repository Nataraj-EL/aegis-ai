package com.aegis.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LogoutEvent extends ApplicationEvent {
    private final String username;

    public LogoutEvent(final Object source, final String username) {
        super(source);
        this.username = username;
    }
}
