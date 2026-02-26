package org.example.tudubem.llm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.llm.dto.GenerateModelResponse;
import org.example.tudubem.llm.dto.GenerateRequest;
import org.example.tudubem.llm.dto.GenerateResponse;
import org.example.tudubem.llm.dto.GenerateStreamResponse;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "LLM 생성 API")
public class OllamaController {
    private final ChatClient ollamaChatClient;

    @PostMapping("/generate")
    @Operation(summary = "단건 생성", description = "입력 메시지를 기반으로 구조화된 생성 결과를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공")
    })
    public Mono<GenerateResponse> generate(@RequestBody GenerateRequest request) {
        String message = resolveMessage(request);
        String conversationId = resolveConversationId(request);

        return Mono.fromCallable(() -> this.ollamaChatClient.prompt()
                        .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                        .user(message)
                        .call()
                        .entity(GenerateModelResponse.class))
                .subscribeOn(Schedulers.boundedElastic())
                .map(content -> new GenerateResponse(conversationId, content.answer(), content.reason()));
    }

    @PostMapping(value = "/generateStream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    @Operation(summary = "스트림 생성", description = "입력 메시지를 기반으로 생성 토큰을 NDJSON 스트림으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스트림 생성 성공")
    })
    public Flux<GenerateStreamResponse> generateStream(@RequestBody GenerateRequest request) {
        String message = resolveMessage(request);
        String conversationId = resolveConversationId(request);

        Flux<GenerateStreamResponse> chunks = this.ollamaChatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message)
                .stream()
                .content()
                .index()
                .map(tuple -> new GenerateStreamResponse(
                        conversationId,
                        tuple.getT1(),
                        tuple.getT2(),
                        false
                ));

        return chunks.concatWithValues(new GenerateStreamResponse(conversationId, -1, "", true));
    }

    private String resolveMessage(GenerateRequest request) {
        if (request != null && request.message() != null && !request.message().isBlank()) {
            return request.message();
        }
        return "가벼운 농담 해줘.";
    }

    private String resolveConversationId(GenerateRequest request) {
        if (request != null && request.conversationId() != null && !request.conversationId().isBlank()) {
            return request.conversationId();
        }
        return UUID.randomUUID().toString();
    }
}
