package org.example.tudubem.world;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.grid.GridMap;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/world")
@RequiredArgsConstructor
@Tag(name = "World", description = "월드 빌드/조회 API")
public class WorldController {

    private final WorldService worldService;

    @PostMapping("/{mapId}/build")
    @Operation(summary = "월드 빌드 및 캐시", description = "지도의 센서맵과 레이어를 기반으로 GridMap을 생성하고 메모리에 캐시합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "빌드 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<GridMap> buildAndCache(@PathVariable Long mapId) {
        try {
            GridMap gridMap = worldService.buildAndCache(mapId);
            return ResponseEntity.ok(gridMap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/{mapId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "캐시된 월드 SSE 스트림", description = "해당 mapId의 GridMap 변경 이벤트를 SSE로 구독합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 성공")
    })
    public Flux<ServerSentEvent<GridMap>> streamGridMap(@PathVariable Long mapId) {
        return worldService.asFlux(mapId)
                .map(gridMap -> ServerSentEvent.<GridMap>builder()
                        .event("grid-map")
                        .data(gridMap)
                        .build());
    }

    @GetMapping(value = "/{mapId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "캐시된 그리드맵 이미지 조회", description = "캐시된 GridMap을 PNG(image/png)로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "캐시된 그리드맵이 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "이미지 변환 실패", content = @Content)
    })
    public ResponseEntity<byte[]> getCachedGridMapImage(@PathVariable Long mapId) {
        return worldService.getCached(mapId)
                .map(WorldUtils::toPngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{mapId}/dynamic-objects")
    @Operation(summary = "동적 객체 단건 업서트", description = "objectId 기준으로 동적 객체를 추가/갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류", content = @Content),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<GridMap> upsertDynamicObject(
            @PathVariable Long mapId,
            @RequestBody DynamicObjectRequest request
    ) {
        try {
            return ResponseEntity.ok(worldService.upsertDynamicObject(mapId, request.objectId(), request.verticesJson()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{mapId}/dynamic-objects/{objectId}")
    @Operation(summary = "동적 객체 제거", description = "objectId 기준으로 동적 객체를 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<GridMap> removeDynamicObject(@PathVariable Long mapId, @PathVariable String objectId) {
        try {
            return ResponseEntity.ok(worldService.removeDynamicObject(mapId, objectId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{mapId}/dynamic-objects")
    @Operation(summary = "동적 객체 전체 제거", description = "해당 mapId의 모든 동적 객체를 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<GridMap> clearDynamicObjects(@PathVariable Long mapId) {
        try {
            return ResponseEntity.ok(worldService.clearDynamicObjects(mapId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
