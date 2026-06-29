package com.aegis.backend.agent;

import com.aegis.backend.ai.AiService;
import com.aegis.backend.ai.PromptManager;
import com.aegis.backend.dto.AgentChatRequest;
import com.aegis.backend.dto.AgentChatResponse;
import com.aegis.backend.dto.KnowledgeIntelligenceReport;
import com.aegis.backend.tool.ToolExecutor;
import com.aegis.backend.util.KnowledgeIntelligenceParser;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KnowledgeIntelligenceAgent implements Agent {

    private static final String KEY_DOCUMENTS_TOTAL = "totalCount";

    private final AiService aiService;
    private final ToolExecutor toolExecutor;
    private final PromptManager promptManager;
    private final KnowledgeIntelligenceParser parser;

    public KnowledgeIntelligenceAgent(
            final AiService aiService,
            final ToolExecutor toolExecutor,
            final PromptManager promptManager,
            final KnowledgeIntelligenceParser parser) {
        this.aiService = aiService;
        this.toolExecutor = toolExecutor;
        this.promptManager = promptManager;
        this.parser = parser;
    }

    @Override
    public String getId() {
        return "knowledge_intelligence";
    }

    @Override
    public String getName() {
        return "Knowledge Intelligence Agent";
    }

    @Override
    public String getDescription() {
        return "Retrieves relevant document context first, then answers strictly from retrieved context with explicit grounding and source citations.";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("knowledge search", "rag retrieval", "semantic auditing", "information synthesis");
    }

    @SuppressWarnings("unchecked")
    @Override
    public AgentChatResponse process(final AgentChatRequest knowledgeRequest, final AgentContext knowledgeContext) {
        final Map<String, Object> toolArguments = new HashMap<>();
        toolArguments.put("query", knowledgeRequest.getMessage());
        toolArguments.put("limit", 5);
        toolArguments.put("minSimilarity", 0.6);

        Map<String, Object> summaryResult;
        try {
            summaryResult = (Map<String, Object>) toolExecutor.execute("knowledge_summary", toolArguments);
        } catch (final Exception exception) {
            log.error("Failed to execute KnowledgeSummaryTool. Activating empty fallback context.", exception);
            summaryResult = new HashMap<>();
            summaryResult.put("totalCount", 0L);
            summaryResult.put("statusCounts", Collections.emptyMap());
            summaryResult.put("documents", Collections.emptyList());
        }

        final Long totalCount = (Long) summaryResult.getOrDefault(KEY_DOCUMENTS_TOTAL, 0L);
        final List<Map<String, Object>> documentsList =
                (List<Map<String, Object>>) summaryResult.getOrDefault("documents", Collections.emptyList());

        // Format RAG context detailing title and source citations
        final String ragContextStr = documentsList.stream()
                .map(item -> String.format(
                        "Document ID: %s\nTitle: %s\nSource: %s\nTags: %s\nContent:\n%s\n---",
                        item.getOrDefault("id", ""),
                        item.getOrDefault("title", ""),
                        item.getOrDefault("source", ""),
                        item.getOrDefault("tags", ""),
                        item.getOrDefault("content", "")))
                .collect(Collectors.joining("\n\n"));

        final String formattedPayload = String.format(
                "Knowledge Base Context Profile:\n- Active Indexed Count: %d\n" + "Retrieved RAG Documents:\n%s\n",
                totalCount,
                ragContextStr.isEmpty() ? "No matching semantically relevant documents retrieved." : ragContextStr);

        final String generatedQuery = knowledgeContext.buildQuery(formattedPayload, knowledgeRequest.getMessage());

        final String systemPrompt = promptManager.loadSystemPrompt("knowledge_intelligence_agent.txt");
        final long startTime = System.currentTimeMillis();

        AgentChatResponse response;
        try {
            final String rawResponse = aiService.generateResponse(systemPrompt, generatedQuery);
            final KnowledgeIntelligenceReport report = parser.parse(rawResponse);
            final long duration = System.currentTimeMillis() - startTime;

            response = AgentChatResponse.builder()
                    .response(report.getMarkdownSummary())
                    .confidence(report.getConfidence())
                    .suggestedActions(report.getCitations())
                    .executionTime(duration)
                    .model("gemini-1.5-flash")
                    .build();
        } catch (final Exception exception) {
            log.error(
                    "Failed to generate or decode knowledge intelligence RAG report. Activating fallback.", exception);
            final long duration = System.currentTimeMillis() - startTime;

            final String rows = documentsList.stream()
                    .map(item -> String.format(
                            "| %s | %s | %s |",
                            item.getOrDefault("title", ""),
                            item.getOrDefault("source", ""),
                            item.getOrDefault("tags", "")))
                    .collect(Collectors.joining("\n"));

            final String fallbackMarkdown = String.format(
                    "# Knowledge Base Query Result (Fallback Mode)\n\n"
                            + "An error occurred compiling details. The following matches were retrieved:\n\n"
                            + "| Title | Source | Tags |\n"
                            + "| :--- | :--- | :--- |\n"
                            + "%s\n\n"
                            + "> [!WARNING]\n"
                            + "> Natural language answers, grounding validations, and inline citations are currently offline.",
                    rows.isEmpty() ? "| None | None | None |" : rows);

            response = AgentChatResponse.builder()
                    .response(fallbackMarkdown)
                    .confidence(0.5)
                    .suggestedActions(
                            Collections.singletonList("Retry semantic knowledge base query when service stabilizes"))
                    .executionTime(duration)
                    .model("fallback-knowledge")
                    .build();
        }

        return response;
    }
}
