package org.example.tudubem.llm.dto;

public record GenerateRequest(
        String message,
        String conversationId
) {
}
