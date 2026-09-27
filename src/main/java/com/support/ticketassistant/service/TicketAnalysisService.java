package com.support.ticketassistant.service;

import com.support.ticketassistant.domain.Ticket;
import com.support.ticketassistant.domain.TicketAnalysis;
import com.support.ticketassistant.domain.TicketStatus;
import com.support.ticketassistant.exception.LlmTimeoutException;
import com.support.ticketassistant.exception.LlmValidationException;
import com.support.ticketassistant.llm.LlmAnalysisRequest;
import com.support.ticketassistant.llm.LlmAnalysisResult;
import com.support.ticketassistant.llm.LlmProvider;
import com.support.ticketassistant.repository.TicketRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketAnalysisService {

    private final TicketRepository ticketRepository;
    private final LlmProvider llmProvider;

    public TicketAnalysisService(TicketRepository ticketRepository, LlmProvider llmProvider) {
        this.ticketRepository = ticketRepository;
        this.llmProvider = llmProvider;
    }

    @Async("llmTaskExecutor")
    public void analyzeAsync(Integer ticketId) {
        Optional<Ticket> maybeTicket = ticketRepository.findById(ticketId);
        if (maybeTicket.isEmpty()) return;

        Ticket ticket = maybeTicket.get();
        ticket.setStatus(TicketStatus.PROCESSING);
        ticket.setUpdatedAt(Instant.now());
        ticketRepository.save(ticket);

        try {
            LlmAnalysisRequest request = new LlmAnalysisRequest(
                    ticket.getSubject(),
                    sanitizeForLlm(ticket.getDescription()),
                    ticket.getPriority(),
                    ticket.getProduct()
            );

            LlmAnalysisResult result = llmProvider.analyze(request);
            validate(result);

            TicketAnalysis analysis = new TicketAnalysis();
            analysis.setCategory(result.category());
            analysis.setSummary(result.summary());
            analysis.setSuggestedResponse(result.suggestedResponse());
            analysis.setRecommendedTeam(result.recommendedTeam());
            analysis.setConfidence(result.confidence());

            ticket.setAnalysis(analysis);
            ticket.setStatus(TicketStatus.COMPLETED);
            ticket.setFailureReason(null);
            ticket.setUpdatedAt(Instant.now());
            ticketRepository.save(ticket);

        } catch (LlmTimeoutException e) {
            markFailed(ticket, "LLM request timed out: " + e.getMessage());
        } catch (LlmValidationException e) {
            markFailed(ticket, "LLM returned invalid output: " + e.getMessage());
        } catch (Exception e) {
            markFailed(ticket, "Unexpected error during analysis: " + e.getMessage());
        }
    }

    private String sanitizeForLlm(String description) {
        if (description == null) return "";
        return description;
    }

    private void validate(LlmAnalysisResult result) {
        List<String> errors = new ArrayList<>();
        if (isBlank(result.category())) errors.add("category is blank");
        if (isBlank(result.summary())) errors.add("summary is blank");
        if (isBlank(result.suggestedResponse())) errors.add("suggestedResponse is blank");
        if (isBlank(result.recommendedTeam())) errors.add("recommendedTeam is blank");
        if (result.confidence() == null || result.confidence() < 0.0 || result.confidence() > 1.0) {
            errors.add("confidence must be between 0.0 and 1.0");
        }
        if (!errors.isEmpty()) {
            throw new LlmValidationException(String.join("; ", errors));
        }
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }

    private void markFailed(Ticket ticket, String reason) {
        ticket.setStatus(TicketStatus.FAILED);
        ticket.setFailureReason(reason);
        ticket.setUpdatedAt(Instant.now());
        ticketRepository.save(ticket);
    }
}
