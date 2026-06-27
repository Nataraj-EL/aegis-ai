package com.aegis.backend.controller;

import com.aegis.backend.agent.OrchestratorAgent;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final OrchestratorAgent orchestratorAgent;

    public AgentController(final OrchestratorAgent orchestratorAgent) {
        this.orchestratorAgent = orchestratorAgent;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AgentChatResponse>> chat(@Valid @RequestBody final AgentChatRequest request) {
        final AgentChatResponse response = orchestratorAgent.process(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Agent response generated successfully"));
    }
}
