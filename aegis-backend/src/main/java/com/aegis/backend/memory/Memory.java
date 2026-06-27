package com.aegis.backend.memory;

public interface Memory {
    void save(String sessionId, String key, Object value);

    Object retrieve(String sessionId, String key);

    void clear(String sessionId);
}
