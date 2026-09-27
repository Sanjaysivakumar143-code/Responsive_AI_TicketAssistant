package com.support.ticketassistant.dto;

import com.support.ticketassistant.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
        @NotBlank(message = "customerId is required") String customerId,
        @NotBlank(message = "subject is required") String subject,
        @NotBlank(message = "description is required") String description,
        @NotNull(message = "priority is required (LOW, MEDIUM, or HIGH)") Priority priority,
        String product
) {}
