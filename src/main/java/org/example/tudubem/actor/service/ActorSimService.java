package org.example.tudubem.actor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tudubem.actor.dto.ActorStatus;
import org.example.tudubem.actor.service.pathfind.GridPoint;
import org.example.tudubem.actor.service.pathfind.PathFindingStrategy;
import org.example.tudubem.actor.service.pathfind.PathResult;
import org.example.tudubem.world.service.WorldService;
import org.example.tudubem.world.service.grid.GridMap;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActorSimService {
    private static final long DEFAULT_ACTOR_ID = 1L;
    private static final Duration MOVE_TICK = Duration.ofMillis(100);

    private final ActorStatusService actorStatusService;
    private final WorldService worldService;
    private final PathFindingStrategy pathFindingStrategy;

    // GridMap 점유정보를 기준으로 경로를 탐색한다.
    public PathResult findPath(Long mapId, int targetX, int targetY) {
        GridPoint currentPoint = actorStatusService.getCurrentPoint(DEFAULT_ACTOR_ID);
        GridMap gridMap = worldService.getCached(mapId)
                .orElseGet(() -> worldService.buildAndCache(mapId));
        return pathFindingStrategy.findPath(gridMap, currentPoint.x(), currentPoint.y(), targetX, targetY);
    }

    // 목표 좌표까지 경로를 계산하고, 경로가 있으면 0.5초마다 Actor 현재 위치를 갱신한다.
    public PathResult move(Long mapId, int targetX, int targetY) {
        PathResult pathResult = findPath(mapId, targetX, targetY);
        if (!pathResult.found() || pathResult.path().size() <= 1) {
            return pathResult;
        }

        List<GridPoint> moveSteps = pathResult.path().subList(1, pathResult.path().size());
        Flux.fromIterable(moveSteps)
                .delayElements(MOVE_TICK)
                .doOnNext(point -> actorStatusService.upsert(new ActorStatus(
                        DEFAULT_ACTOR_ID,
                        point.x(),
                        point.y(),
                        "moving"
                )))
                .doOnComplete(() -> {
                    GridPoint lastPoint = moveSteps.getLast();
                    actorStatusService.upsert(new ActorStatus(
                            DEFAULT_ACTOR_ID,
                            lastPoint.x(),
                            lastPoint.y(),
                            "arrived"
                    ));
                })
                .doOnError(e -> log.warn("Actor move sequence failed", e))
                .subscribe();

        return pathResult;
    }
}
