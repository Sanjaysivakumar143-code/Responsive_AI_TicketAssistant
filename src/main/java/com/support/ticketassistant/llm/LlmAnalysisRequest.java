package com.support.ticketassistant.llm;

import com.support.ticketassistant.domain.Priority;

public record LlmAnalysisRequest(
        String subject,
        String description,
        Priority priority,
        String product
) {}
