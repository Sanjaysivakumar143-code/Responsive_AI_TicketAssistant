package com.support.ticketassistant.llm;

import com.support.ticketassistant.domain.Priority;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmProvider implements LlmProvider {

    private static final Map<String, String[]> KEYWORD_RULES = new LinkedHashMap<>();
    static {
        KEYWORD_RULES.put("import", new String[]{"IMPORT_FAILURE", "IMPORT_ENGINEERING"});
        KEYWORD_RULES.put("export", new String[]{"EXPORT_FAILURE", "IMPORT_ENGINEERING"});
        KEYWORD_RULES.put("password", new String[]{"ACCOUNT_ACCESS", "ACCOUNT_SECURITY"});
        KEYWORD_RULES.put("login", new String[]{"ACCOUNT_ACCESS", "ACCOUNT_SECURITY"});
        KEYWORD_RULES.put("crash", new String[]{"APPLICATION_CRASH", "PLATFORM_ENGINEERING"});
        KEYWORD_RULES.put("error", new String[]{"APPLICATION_ERROR", "PLATFORM_ENGINEERING"});
        KEYWORD_RULES.put("bill", new String[]{"BILLING_ISSUE", "BILLING"});
        KEYWORD_RULES.put("invoice", new String[]{"BILLING_ISSUE", "BILLING"});
        KEYWORD_RULES.put("charge", new String[]{"BILLING_ISSUE", "BILLING"});
        KEYWORD_RULES.put("slow", new String[]{"PERFORMANCE_ISSUE", "PLATFORM_ENGINEERING"});
    }

    @Override
    public LlmAnalysisResult analyze(LlmAnalysisRequest request) {
        String haystack = (safe(request.subject()) + " " + safe(request.description())).toLowerCase();

        String category = "GENERAL_INQUIRY";
        String team = "GENERAL_SUPPORT";
        for (Map.Entry<String, String[]> rule : KEYWORD_RULES.entrySet()) {
            if (haystack.contains(rule.getKey())) {
                category = rule.getValue()[0];
                team = rule.getValue()[1];
                break;
            }
        }

        double confidence = category.equals("GENERAL_INQUIRY") ? 0.55 : 0.85;
        if (request.priority() == Priority.HIGH) {
            confidence = Math.min(1.0, confidence + 0.05);
        }

        String summary = "Customer reported an issue with subject \"%s\"%s.".formatted(
                safe(request.subject()),
                (request.product() != null && !request.product().isBlank()) ? " related to " + request.product() : ""
        );

        String suggestedResponse = ("Thank you for reaching out. We've received your report and our %s team "
                + "is looking into it. We'll follow up as soon as we have an update.")
                .formatted(team.replace('_', ' ').toLowerCase());

        return new LlmAnalysisResult(category, summary, suggestedResponse, team, roundTo2(confidence));
    }

    private String safe(String value) { return value == null ? "" : value; }

    private double roundTo2(double value) { return Math.round(value * 100.0) / 100.0; }
}
