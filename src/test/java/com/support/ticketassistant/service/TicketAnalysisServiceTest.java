package com.support.ticketassistant.service;

import com.support.ticketassistant.domain.Priority;
import com.support.ticketassistant.domain.Ticket;
import com.support.ticketassistant.domain.TicketStatus;
import com.support.ticketassistant.exception.LlmTimeoutException;
import com.support.ticketassistant.llm.LlmAnalysisResult;
import com.support.ticketassistant.llm.LlmProvider;
import com.support.ticketassistant.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TicketAnalysisServiceTest {

    private TicketRepository ticketRepository;
    private LlmProvider llmProvider;
    private TicketAnalysisService service;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        llmProvider = mock(LlmProvider.class);
        service = new TicketAnalysisService(ticketRepository, llmProvider);
    }

    private Ticket sampleTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setCustomerId("CUST-1");
        ticket.setSubject("Cannot import file");
        ticket.setDescription("Import stuck at 80 percent");
        ticket.setPriority(Priority.HIGH);
        ticket.setProduct("Import");
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticket;
    }

    @Test
    void successfulAnalysis_marksTicketCompleted() {
        Ticket ticket = sampleTicket();
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(llmProvider.analyze(any())).thenReturn(new LlmAnalysisResult(
                "IMPORT_FAILURE", "Import fails at 80 percent", "We are investigating.", "IMPORT_ENGINEERING", 0.9));

        service.analyzeAsync(ticket.getId());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, atLeastOnce()).save(captor.capture());
        Ticket finalState = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertEquals(TicketStatus.COMPLETED, finalState.getStatus());
        assertNotNull(finalState.getAnalysis());
        assertEquals("IMPORT_FAILURE", finalState.getAnalysis().getCategory());
        assertNull(finalState.getFailureReason());
    }

    @Test
    void invalidLlmOutput_marksTicketFailed() {
        Ticket ticket = sampleTicket();
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(llmProvider.analyze(any())).thenReturn(new LlmAnalysisResult(
                "", "summary", "response", "TEAM", 1.5));

        service.analyzeAsync(ticket.getId());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, atLeastOnce()).save(captor.capture());
        Ticket finalState = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertEquals(TicketStatus.FAILED, finalState.getStatus());
        assertNotNull(finalState.getFailureReason());
    }

    @Test
    void llmTimeout_marksTicketFailed() {
        Ticket ticket = sampleTicket();
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(llmProvider.analyze(any())).thenThrow(new LlmTimeoutException("simulated timeout"));

        service.analyzeAsync(ticket.getId());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, atLeastOnce()).save(captor.capture());
        Ticket finalState = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertEquals(TicketStatus.FAILED, finalState.getStatus());
        assertTrue(finalState.getFailureReason().contains("timed out"));
    }
}
