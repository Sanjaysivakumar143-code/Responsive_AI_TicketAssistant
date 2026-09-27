package com.support.ticketassistant.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticketassistant.exception.LlmTimeoutException;
import com.support.ticketassistant.exception.LlmValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "openai")
public class OpenAiLlmProvider implements LlmProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String promptTemplate;

    public OpenAiLlmProvider(
            @Value("${llm.openai.base-url}") String baseUrl,
            @Value("${llm.openai.api-key}") String apiKey,
            @Value("${llm.openai.model}") String model,
            @Value("${llm.timeout-ms}") long timeoutMs,
            ObjectMapper objectMapper) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) timeoutMs);
        requestFactory.setReadTimeout((int) timeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(requestFactory)
                .build();
        this.model = model;
        this.objectMapper = objectMapper;
        this.promptTemplate = loadPromptTemplate();
    }

    private String loadPromptTemplate() {
        try (InputStream in = getClass().getResourceAsStream("/prompts/ticket-analysis-prompt.txt")) {
            if (in == null) throw new IllegalStateException("Prompt template not found on classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load prompt template", e);
        }
    }

    @Override
    @Retryable(retryFor = LlmTimeoutException.class, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    public LlmAnalysisResult analyze(LlmAnalysisRequest request) {
        String prompt = promptTemplate
                .replace("{{subject}}", safe(request.subject()))
                .replace("{{priority}}", String.valueOf(request.priority()))
                .replace("{{product}}", safe(request.product()))
                .replace("{{description}}", safe(request.description()));

        Map<String, Object> body = Map.of(
                "model", model,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        String rawJson;
        try {
            OpenAiChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new LlmValidationException("LLM provider returned no choices");
            }
            rawJson = response.choices().get(0).message().content();
        } catch (ResourceAccessException e) {
            throw new LlmTimeoutException("Timed out calling LLM provider", e);
        } catch (RestClientResponseException e) {
            throw new LlmValidationException("LLM provider returned an error: " + e.getStatusCode());
        }

        try {
            return objectMapper.readValue(rawJson, LlmAnalysisResult.class);
        } catch (Exception e) {
            throw new LlmValidationException("LLM response was not valid JSON: " + e.getMessage());
        }
    }

    private String safe(String value) { return value == null ? "" : value; }
}
