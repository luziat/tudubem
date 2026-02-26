package org.example.tudubem.actor;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.actor.pathfind.GridPoint;
import org.example.tudubem.actor.pathfind.PathResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actor/sim")
@RequiredArgsConstructor
@Tag(name = "ActorSim", description = "Actor 시뮬레이션 API")
public class ActorSimController {
    private static final long DEFAULT_ACTOR_ID = 1L;

    private final ActorSimService actorSimService;

    @PostMapping("/{mapId}")
    public ResponseEntity<?> move(@PathVariable Long mapId, @RequestBody GridPoint gridPoint) {
        PathResult path = actorSimService.move(mapId, gridPoint.x(), gridPoint.y());
        return ResponseEntity.ok(path);
    }

    @GetMapping(value = "/{mapId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> trajectoryImage(
            @PathVariable Long mapId,
            @RequestParam(required = false) Long actorId
    ) {
        long targetActorId = actorId == null ? DEFAULT_ACTOR_ID : actorId;
        return actorSimService.getTrajectoryImage(mapId, targetActorId);
    }
}
