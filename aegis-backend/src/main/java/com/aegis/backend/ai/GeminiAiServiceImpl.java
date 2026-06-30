package com.aegis.backend.ai;

import com.aegis.backend.service.MetricsService;
import java.util.List;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class GeminiAiServiceImpl implements AiService {

    private final ChatModel chatModel;
    private final MetricsService metricsService;

    public GeminiAiServiceImpl(final ChatModel chatModel, final MetricsService metricsService) {
        this.chatModel = chatModel;
        this.metricsService = metricsService;
    }

    @Override
    public String generateResponse(final String systemPrompt, final String userMessage) {
        final SystemMessage systemMessage = new SystemMessage(systemPrompt);
        final UserMessage userMsg = new UserMessage(userMessage);
        final Prompt prompt = new Prompt(List.of(systemMessage, userMsg));

        final long startTime = System.currentTimeMillis();
        try {
            final String response =
                    chatModel.call(prompt).getResult().getOutput().getText();
            final long duration = System.currentTimeMillis() - startTime;
            metricsService.recordAiRequest("gemini-1.5-flash", "success", duration);
            return response;
        } catch (final Exception exception) {
            final long duration = System.currentTimeMillis() - startTime;
            metricsService.recordAiRequest("gemini-1.5-flash", "failure", duration);
            throw exception;
        }
    }
}
