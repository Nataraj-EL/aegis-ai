package com.aegis.backend.agent;

import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import java.util.List;

public interface Agent {
    String getId();

    String getName();

    String getDescription();

    List<String> getCapabilities();

    AgentChatResponse process(AgentChatRequest request);
}
