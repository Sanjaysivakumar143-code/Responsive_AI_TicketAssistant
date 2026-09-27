package com.support.ticketassistant.llm;

public record LlmAnalysisResult(
        String category,
        String summary,
        String suggestedResponse,
        String recommendedTeam,
        Double confidence
) {}
