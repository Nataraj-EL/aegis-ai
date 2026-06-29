package com.aegis.backend.event;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class KnowledgeDocumentSavedEvent extends ApplicationEvent {
    private final UUID documentId;
    private final String content;

    public KnowledgeDocumentSavedEvent(final Object source, final UUID documentId, final String content) {
        super(source);
        this.documentId = documentId;
        this.content = content;
    }
}
