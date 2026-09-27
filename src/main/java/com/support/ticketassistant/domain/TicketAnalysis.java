package com.support.ticketassistant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TicketAnalysis {

    private String category;

    @Column(length = 1000)
    private String summary;

    @Column(length = 2000)
    private String suggestedResponse;

    private String recommendedTeam;

    private Double confidence;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSuggestedResponse() { return suggestedResponse; }
    public void setSuggestedResponse(String suggestedResponse) { this.suggestedResponse = suggestedResponse; }

    public String getRecommendedTeam() { return recommendedTeam; }
    public void setRecommendedTeam(String recommendedTeam) { this.recommendedTeam = recommendedTeam; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}
