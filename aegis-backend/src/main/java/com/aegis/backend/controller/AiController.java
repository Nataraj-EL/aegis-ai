package com.aegis.backend.controller;

import com.aegis.backend.ai.AiProvider;
import com.aegis.backend.config.AiProperties;
import com.aegis.backend.dto.AiProvidersResponse;
import com.aegis.backend.dto.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class AiController {

    private final List<AiProvider> providers;
    private final AiProperties aiProperties;

    public AiController(final List<AiProvider> providers, final AiProperties aiProperties) {
        this.providers = providers;
        this.aiProperties = aiProperties;
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<AiProvidersResponse>> getProviders() {
        final List<AiProvidersResponse.ProviderInfo> list = providers.stream()
                .map(provider -> {
                    final String name = provider.getProviderName();
                    final AiProperties.ProviderConfig config =
                            aiProperties.getProviders().get(name);
                    final String model = config != null ? config.getModel() : "unknown";
                    return AiProvidersResponse.ProviderInfo.builder()
                            .name(name)
                            .model(model)
                            .capabilities(provider.getCapabilities())
                            .health(provider.healthCheck())
                            .build();
                })
                .collect(Collectors.toList());

        final AiProvidersResponse response = AiProvidersResponse.builder()
                .activeProvider(aiProperties.getProvider())
                .providers(list)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "AI providers loaded successfully"));
    }
}
