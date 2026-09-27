package com.support.ticketassistant.llm;

import java.util.List;

public record OpenAiChatResponse(List<Choice> choices) {
    public record Choice(Message message) {}
    public record Message(String content) {}
}
