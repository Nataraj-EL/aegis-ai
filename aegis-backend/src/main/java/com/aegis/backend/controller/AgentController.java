package com.aegis.backend.controller;

import com.aegis.backend.agent.OrchestratorAgent;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Agent Controller", description = "Protected REST API endpoints for communicating with Aegis AI agents")
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final OrchestratorAgent orchestratorAgent;

    public AgentController(final OrchestratorAgent orchestratorAgent) {
        this.orchestratorAgent = orchestratorAgent;
    }

    @Operation(
            summary = "Send a query to the agentic orchestrator",
            description =
                    "Protected endpoint to route operational messages, metadata queries, or executive insights requests to the appropriate agent context.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Successfully processed the request and generated the agent response"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Missing or invalid Bearer JWT token credentials")
    })
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AgentChatResponse>> chat(@Valid @RequestBody final AgentChatRequest request) {
        final AgentChatResponse response = orchestratorAgent.process(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Agent response generated successfully"));
    }
}
