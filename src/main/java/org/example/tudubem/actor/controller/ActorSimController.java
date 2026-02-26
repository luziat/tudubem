package org.example.tudubem.actor.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.actor.service.ActorSimService;
import org.example.tudubem.actor.service.pathfind.GridPoint;
import org.example.tudubem.actor.service.pathfind.PathResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actor/sim")
@RequiredArgsConstructor
@Tag(name = "ActorSim", description = "Actor 시뮬레이션 API")
public class ActorSimController {
    private final ActorSimService actorSimService;

    @PostMapping("/{mapId}")
    public ResponseEntity<?> move(@PathVariable Long mapId, @RequestBody GridPoint gridPoint) {
        PathResult path = actorSimService.move(mapId, gridPoint.x(), gridPoint.y());
        return ResponseEntity.ok(path);
    }
}
