package org.example.tudubem.llm.dto;

public record GenerateResponse(
        String conversationId,
        String generation,
        String reason
) {
}
