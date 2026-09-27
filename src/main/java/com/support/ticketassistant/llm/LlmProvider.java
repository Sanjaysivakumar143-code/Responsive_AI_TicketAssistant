package com.support.ticketassistant.llm;

public interface LlmProvider {
    LlmAnalysisResult analyze(LlmAnalysisRequest request);
}
