package com.aegis.backend.ai;

import java.util.List;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class GeminiAiServiceImpl implements AiService {

    private final ChatModel chatModel;

    public GeminiAiServiceImpl(final ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generateResponse(final String systemPrompt, final String userMessage) {
        final SystemMessage systemMessage = new SystemMessage(systemPrompt);
        final UserMessage userMsg = new UserMessage(userMessage);
        final Prompt prompt = new Prompt(List.of(systemMessage, userMsg));
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
