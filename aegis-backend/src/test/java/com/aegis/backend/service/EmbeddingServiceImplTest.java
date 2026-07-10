package com.aegis.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aegis.backend.ai.*;
import com.aegis.backend.config.AiProperties;
import com.aegis.backend.config.FallbackStrategy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmbeddingServiceImplTest {

    private AiProperties aiProperties;
    private AiProvider geminiProvider;
    private AiProvider openaiProvider;

    @BeforeEach
    public void setUp() {
        aiProperties = mock(AiProperties.class);
        geminiProvider = mock(AiProvider.class);
        openaiProvider = mock(AiProvider.class);

        when(geminiProvider.getProviderName()).thenReturn("gemini");
        when(openaiProvider.getProviderName()).thenReturn("openai");

        when(geminiProvider.healthCheck())
                .thenReturn(ProviderHealth.builder()
                        .status("UP")
                        .message("OK")
                        .latencyMs(10L)
                        .timestamp(System.currentTimeMillis())
                        .build());

        when(openaiProvider.healthCheck())
                .thenReturn(ProviderHealth.builder()
                        .status("UP")
                        .message("OK")
                        .latencyMs(10L)
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    @Test
    public void testGenerateEmbeddingSuccess() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        when(geminiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));

        final float[] expectedEmbedding = new float[] {0.1f, 0.2f};
        when(geminiProvider.generateEmbedding("test text")).thenReturn(expectedEmbedding);

        final EmbeddingServiceImpl embeddingService =
                new EmbeddingServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties);

        final float[] result = embeddingService.generateEmbedding("test text");
        assertArrayEquals(expectedEmbedding, result);
        verify(geminiProvider, times(1)).generateEmbedding("test text");
    }

    @Test
    public void testGenerateEmbeddingSkipsProvidersWithoutEmbeddingCapability() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        // Gemini only supports CHAT, not EMBEDDING
        when(geminiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.CHAT));
        // OpenAI supports EMBEDDING
        when(openaiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));

        final float[] expectedEmbedding = new float[] {0.5f, 0.6f};
        when(openaiProvider.generateEmbedding("test text")).thenReturn(expectedEmbedding);

        final EmbeddingServiceImpl embeddingService =
                new EmbeddingServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties);

        final float[] result = embeddingService.generateEmbedding("test text");
        assertArrayEquals(expectedEmbedding, result);
        verify(geminiProvider, never()).generateEmbedding(anyString());
        verify(openaiProvider, times(1)).generateEmbedding("test text");
    }

    @Test
    public void testGenerateEmbeddingSkipsDownProviders() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        when(geminiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));
        when(openaiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));

        // Gemini is DOWN
        when(geminiProvider.healthCheck())
                .thenReturn(ProviderHealth.builder()
                        .status("DOWN")
                        .message("Unhealthy")
                        .build());

        final float[] expectedEmbedding = new float[] {0.9f};
        when(openaiProvider.generateEmbedding("test text")).thenReturn(expectedEmbedding);

        final EmbeddingServiceImpl embeddingService =
                new EmbeddingServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties);

        final float[] result = embeddingService.generateEmbedding("test text");
        assertArrayEquals(expectedEmbedding, result);
        verify(geminiProvider, never()).generateEmbedding(anyString());
        verify(openaiProvider, times(1)).generateEmbedding("test text");
    }

    @Test
    public void testGenerateEmbeddingFallbackOnException() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        when(geminiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));
        when(openaiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));

        // Gemini throws exception
        when(geminiProvider.generateEmbedding(anyString())).thenThrow(new RuntimeException("Failure"));

        final float[] expectedEmbedding = new float[] {0.9f};
        when(openaiProvider.generateEmbedding("test text")).thenReturn(expectedEmbedding);

        final EmbeddingServiceImpl embeddingService =
                new EmbeddingServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties);

        final float[] result = embeddingService.generateEmbedding("test text");
        assertArrayEquals(expectedEmbedding, result);
        verify(geminiProvider, times(1)).generateEmbedding("test text");
        verify(openaiProvider, times(1)).generateEmbedding("test text");
    }

    @Test
    public void testGenerateEmbeddingAllFailedThrowsException() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("gemini", "openai"));

        when(geminiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));
        when(openaiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));

        when(geminiProvider.generateEmbedding(anyString())).thenThrow(new RuntimeException("Failure"));
        when(openaiProvider.generateEmbedding(anyString())).thenThrow(new RuntimeException("Failure"));

        final EmbeddingServiceImpl embeddingService =
                new EmbeddingServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties);

        assertThrows(IllegalStateException.class, () -> embeddingService.generateEmbedding("test text"));
    }

    @Test
    public void testGenerateEmbeddingUnknownProviderInChainSkipped() {
        when(aiProperties.getProvider()).thenReturn("gemini");
        when(aiProperties.getFallbackStrategy()).thenReturn(FallbackStrategy.FAILOVER);
        when(aiProperties.getFallbackChain()).thenReturn(List.of("unknown-provider", "openai"));

        when(openaiProvider.getCapabilities()).thenReturn(List.of(AiProviderCapability.EMBEDDING));
        final float[] expectedEmbedding = new float[] {0.9f};
        when(openaiProvider.generateEmbedding("test text")).thenReturn(expectedEmbedding);

        final EmbeddingServiceImpl embeddingService =
                new EmbeddingServiceImpl(List.of(geminiProvider, openaiProvider), aiProperties);

        final float[] result = embeddingService.generateEmbedding("test text");
        assertArrayEquals(expectedEmbedding, result);
        verify(openaiProvider, times(1)).generateEmbedding("test text");
    }
}
