package com.support.ticketassistant.service;

import com.support.ticketassistant.domain.Ticket;
import com.support.ticketassistant.domain.TicketStatus;
import com.support.ticketassistant.dto.CreateTicketRequest;
import com.support.ticketassistant.exception.TicketNotFoundException;
import com.support.ticketassistant.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketAnalysisService ticketAnalysisService;

    public TicketService(TicketRepository ticketRepository, TicketAnalysisService ticketAnalysisService) {
        this.ticketRepository = ticketRepository;
        this.ticketAnalysisService = ticketAnalysisService;
    }

    public Ticket createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(request.customerId());
        ticket.setSubject(request.subject());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setProduct(request.product());
        ticket.setStatus(TicketStatus.PENDING);

        Instant now = Instant.now();
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket); // id is populated by the DB after this line
        ticketAnalysisService.analyzeAsync(saved.getId());
        return saved;
    }

    public Ticket getTicket(Integer ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }
}