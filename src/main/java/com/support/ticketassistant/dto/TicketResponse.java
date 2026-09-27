package com.support.ticketassistant.dto;

import com.support.ticketassistant.domain.Priority;
import com.support.ticketassistant.domain.Ticket;
import com.support.ticketassistant.domain.TicketStatus;

import java.time.Instant;

public record TicketResponse(
        Integer id,
        String customerId,
        String subject,
        String description,
        Priority priority,
        String product,
        TicketStatus status,
        String failureReason,
        AnalysisResponse analysis,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getCustomerId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getProduct(),
                ticket.getStatus(),
                ticket.getFailureReason(),
                AnalysisResponse.from(ticket.getAnalysis()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}