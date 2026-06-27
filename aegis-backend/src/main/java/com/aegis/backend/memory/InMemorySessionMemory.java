package com.aegis.backend.memory;

import com.aegis.backend.dto.ChatMessageDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemorySessionMemory implements ConversationMemory {

    private final Map<String, List<ChatMessageDto>> store = new ConcurrentHashMap<>();

    public InMemorySessionMemory() {
        // Default constructor
    }

    @Override
    public void save(final String sessionId, final String key, final Object value) {
        // Basic key value storage (not used for simple conversation tracking)
    }

    @Override
    public Object retrieve(final String sessionId, final String key) {
        return null;
    }

    @Override
    public void addMessage(final String sessionId, final String role, final String content) {
        store.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new ChatMessageDto(role, content));
    }

    @Override
    public List<ChatMessageDto> getMessages(final String sessionId) {
        return store.getOrDefault(sessionId, Collections.emptyList());
    }

    @Override
    public void clear(final String sessionId) {
        store.remove(sessionId);
    }
}
