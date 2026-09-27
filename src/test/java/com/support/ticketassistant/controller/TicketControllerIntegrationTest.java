package com.support.ticketassistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticketassistant.domain.Priority;
import com.support.ticketassistant.dto.CreateTicketRequest;
import com.support.ticketassistant.dto.TicketResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void createTicket_thenAnalysisEventuallyCompletes() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
                "CUST-1", "Cannot import file", "The import stalls at 80 percent and then fails.",
                Priority.HIGH, "Import");

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        TicketResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), TicketResponse.class);

        await().atMost(5, TimeUnit.SECONDS).pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            MvcResult getResult = mockMvc.perform(get("/api/tickets/" + created.id())).andReturn();
            TicketResponse current = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), TicketResponse.class);
            assertEquals("COMPLETED", current.status().name());
            assertNotNull(current.analysis());
            assertTrue(current.analysis().confidence() >= 0.0 && current.analysis().confidence() <= 1.0);
        });
    }

    @Test
    void createTicket_missingRequiredFields_returns400() throws Exception {
        String invalidJson = "{\"customerId\":\"\",\"subject\":\"\",\"description\":\"\"}";

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTicket_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/tickets/999999"))
                .andExpect(status().isNotFound());
    }
}
