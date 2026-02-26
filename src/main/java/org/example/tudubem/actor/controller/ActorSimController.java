package org.example.tudubem.actor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.actor.dto.ActorStatus;
import org.example.tudubem.actor.service.ActorSimService;
import org.example.tudubem.actor.service.ActorStatusService;
import org.example.tudubem.actor.service.pathfind.GridPoint;
import org.example.tudubem.actor.service.pathfind.PathResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/actor/sim")
@RequiredArgsConstructor
@Tag(name = "ActorSim", description = "Actor 시뮬레이션 API")
public class ActorSimController {
    private final ActorSimService actorSimService;
    private final ActorStatusService actorStatusService;

    @PostMapping("/{mapId}")
    @Operation(summary = "Actor 이동 시뮬레이션", description = "지정한 actorId의 현재 위치에서 목표 좌표까지 경로를 계산하고, 경로를 따라 상태를 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "지도 또는 actor 상태를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<?> move(
            @Parameter(description = "지도 ID", example = "1")
            @PathVariable Long mapId,
            @Parameter(description = "이동시킬 Actor ID", example = "1")
            @RequestParam Long actorId,
            @RequestBody GridPoint gridPoint
    ) {
        PathResult path = actorSimService.move(mapId, actorId, gridPoint.x(), gridPoint.y());
        return ResponseEntity.ok(path);
    }

    @GetMapping(value = "/status/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Actor 상태 SSE 스트림", description = "모든 Actor 상태 변경 이벤트를 SSE로 구독합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 성공")
    })
    public Flux<ServerSentEvent<ActorStatus>> statusStream() {
        return actorStatusService.asFlux()
                .map(status -> ServerSentEvent.<ActorStatus>builder()
                        .event("actor-status")
                        .data(status)
                        .build());
    }
}
