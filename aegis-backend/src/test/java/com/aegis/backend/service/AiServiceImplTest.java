package com.aegis.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aegis.backend.ai.*;
import com.aegis.backend.config.AiProperties;
import com.aegis.backend.config.FallbackStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class AiServiceImplTest {

    private MetricsService metricsService;
    private AiProperties aiProperties;
    private AiProvider geminiProvider;
    private AiProvider openaiProvider;
    private Map<String, AiProperties.ProviderConfig> providerConfigs;

    @BeforeEach
    public void setUp() {
        metricsService = mock(MetricsService.class);
        aiProperties = mock(AiProperties.class);
        geminiProvider = mock(AiProvider.class);
        openaiProvider = mock(AiProvider.class);

        when(geminiProvider.getProviderName()).thenReturn("gemini");
        when(openaiProvider.getProviderName()).thenReturn("openai");

        when(geminiProvider.healthCheck())
                .thenReturn(ProviderHealth.builder()
                        .status("UP")
                        .message("OK")
                        .latencyMs(5L)
                        .timestamp(System.currentTimeMillis())
                        .build());

        when(openaiProvider.healthCheck())
                .thenReturn(ProviderHealth.builder()
                        .status("UP")
                        .message("OK")
                        .latencyMs(5L)
                        .timestamp(System.currentTimeMillis())
                        .build());

        providerConfigs = new HashMap<>();
        final AiProperties.ProviderConfig geminiConfig = new AiProperties.ProviderConfig();
        geminiConfig.setRetries(1);
        providerConfigs.put("gemini", geminiConfig);

        final AiProperties.ProviderConfig openaiConfig = new AiProperties.ProviderConfig();
        openaiConfig.setRetries(1);
        providerConfigs.put("openai", openaiConfig);

        when(aiProperties.getProviders()).thenReturn(providerConfigs);
    }

    @Test
    public void testGenerateResponseSuccess() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        when(geminiProvider.chat(anyString(), anyString())).thenReturn("Gemini success response");

        final AiServiceImpl aiService =
                new AiServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties, metricsService);

        final String response = aiService.generateResponse("sys", "user");
        assertEquals("Gemini success response", response);
        verify(geminiProvider, times(1)).chat("sys", "user");
        verify(metricsService, times(1)).recordAiProviderRequest(eq("gemini"), eq("success"), anyLong());
    }

    @Test
    public void testGenerateResponseFallbackOnTransientFailure() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        // Gemini throws transient error (e.g. 500 Server Error)
        when(geminiProvider.chat(anyString(), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        // OpenAI succeeds
        when(openaiProvider.chat(anyString(), anyString())).thenReturn("OpenAI fallback success");

        final AiServiceImpl aiService =
                new AiServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties, metricsService);

        final String response = aiService.generateResponse("sys", "user");
        assertEquals("OpenAI fallback success", response);
        verify(geminiProvider, times(2)).chat("sys", "user"); // 1 initial + 1 retry
        verify(openaiProvider, times(1)).chat("sys", "user");
        verify(metricsService, times(2)).recordAiProviderRequest(eq("gemini"), eq("failure"), anyLong());
        verify(metricsService, times(1)).recordAiProviderRequest(eq("openai"), eq("success"), anyLong());
    }

    @Test
    public void testGenerateResponseNoFallbackOnNonTransientFailure() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        // Gemini throws non-transient error (e.g. 401 Unauthorized)
        when(geminiProvider.chat(anyString(), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        // OpenAI succeeds
        when(openaiProvider.chat(anyString(), anyString())).thenReturn("OpenAI fallback success");

        final AiServiceImpl aiService =
                new AiServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties, metricsService);

        final String response = aiService.generateResponse("sys", "user");
        assertEquals("OpenAI fallback success", response);
        // Since it is non-transient, it must immediately failover to OpenAI without retries
        verify(geminiProvider, times(1)).chat("sys", "user");
        verify(openaiProvider, times(1)).chat("sys", "user");
    }

    @Test
    public void testGenerateResponseDownSkipped() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        // Gemini health check is DOWN
        when(geminiProvider.healthCheck())
                .thenReturn(ProviderHealth.builder()
                        .status("DOWN")
                        .message("Service unavailable")
                        .latencyMs(0L)
                        .timestamp(System.currentTimeMillis())
                        .build());

        // OpenAI succeeds
        when(openaiProvider.chat(anyString(), anyString())).thenReturn("OpenAI response");

        final AiServiceImpl aiService =
                new AiServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties, metricsService);

        final String response = aiService.generateResponse("sys", "user");
        assertEquals("OpenAI response", response);
        // Gemini must be skipped entirely because it is DOWN
        verify(geminiProvider, never()).chat(anyString(), anyString());
        verify(openaiProvider, times(1)).chat("sys", "user");
    }

    @Test
    public void testGenerateResponsePrimaryOnly() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.PRIMARY_ONLY);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini"));

        when(geminiProvider.chat(anyString(), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        final AiServiceImpl aiService =
                new AiServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties, metricsService);

        assertThrows(IllegalStateException.class, () -> aiService.generateResponse("sys", "user"));
        // Since strategy is PRIMARY_ONLY, it should not fall back to openai
        verify(openaiProvider, never()).chat(anyString(), anyString());
    }

    @Test
    public void testGenerateResponseAllFailed() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        when(geminiProvider.chat(anyString(), anyString())).thenThrow(new RuntimeException("Gemini failed"));
        when(openaiProvider.chat(anyString(), anyString())).thenThrow(new RuntimeException("OpenAI failed"));

        final AiServiceImpl aiService =
                new AiServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties, metricsService);

        assertThrows(IllegalStateException.class, () -> aiService.generateResponse("sys", "user"));
    }

    @Test
    public void testGenerateResponseUnknownProviderInChainSkipped() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("unknown-provider", "openai"));

        when(openaiProvider.chat(anyString(), anyString())).thenReturn("OpenAI response");

        final AiServiceImpl aiService =
                new AiServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties, metricsService);

        final String response = aiService.generateResponse("sys", "user");
        assertEquals("OpenAI response", response);
        verify(openaiProvider, times(1)).chat("sys", "user");
    }
}
