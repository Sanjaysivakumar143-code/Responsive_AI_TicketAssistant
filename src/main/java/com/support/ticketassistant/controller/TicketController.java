package com.support.ticketassistant.controller;

import com.support.ticketassistant.domain.Ticket;
import com.support.ticketassistant.dto.CreateTicketRequest;
import com.support.ticketassistant.dto.TicketResponse;
import com.support.ticketassistant.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Integer id) {
        Ticket ticket = ticketService.getTicket(id);
        return ResponseEntity.ok(TicketResponse.from(ticket));
    }
}