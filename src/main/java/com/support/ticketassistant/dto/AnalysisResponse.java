package com.support.ticketassistant.dto;

import com.support.ticketassistant.domain.TicketAnalysis;

public record AnalysisResponse(
        String category,
        String summary,
        String suggestedResponse,
        String recommendedTeam,
        Double confidence
) {
    public static AnalysisResponse from(TicketAnalysis analysis) {
        if (analysis == null || analysis.getCategory() == null) {
            return null;
        }
        return new AnalysisResponse(
                analysis.getCategory(),
                analysis.getSummary(),
                analysis.getSuggestedResponse(),
                analysis.getRecommendedTeam(),
                analysis.getConfidence()
        );
    }
}
