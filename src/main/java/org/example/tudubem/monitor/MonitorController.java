package org.example.tudubem.monitor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("monitor")
@RequestMapping("/monitor")
@RequiredArgsConstructor
@Tag(name = "Monitor", description = "모니터링 시각화 API")
public class MonitorController {

    private static final long DEFAULT_ACTOR_ID = 1L;
    private final MonitorService monitorService;

    @GetMapping(value = "/{mapId}/trajectory-image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Actor 궤적 이미지 조회", description = "지정한 actor의 이동 궤적과 현재 위치를 GridMap에 오버레이한 PNG를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음")
    })
    public ResponseEntity<byte[]> trajectoryImage(
            @Parameter(description = "지도 ID", example = "1")
            @PathVariable Long mapId,
            @Parameter(description = "Actor ID (미지정 시 1)", example = "1")
            @RequestParam(required = false) Long actorId
    ) {
        long targetActorId = actorId == null ? DEFAULT_ACTOR_ID : actorId;
        return monitorService.getTrajectoryImage(mapId, targetActorId);
    }
}
