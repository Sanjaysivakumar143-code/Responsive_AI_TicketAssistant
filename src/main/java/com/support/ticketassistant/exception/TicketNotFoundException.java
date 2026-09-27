package com.support.ticketassistant.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Integer ticketId) {
        super("Ticket not found: " + ticketId);
    }
}