package org.example.tudubem.llm.dto;

public record GenerateStreamResponse(
        String conversationId,
        long index,
        String delta,
        boolean done
) {
}
