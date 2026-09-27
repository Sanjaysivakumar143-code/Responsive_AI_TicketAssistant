package com.support.ticketassistant.repository;

import com.support.ticketassistant.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {}