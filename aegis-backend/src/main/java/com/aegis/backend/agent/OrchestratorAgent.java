package com.aegis.backend.agent;

import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;

public interface OrchestratorAgent {
    AgentChatResponse process(AgentChatRequest request);
}
