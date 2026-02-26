package org.example.tudubem.monitor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.tudubem.actor.dto.ActorStatus;
import org.example.tudubem.actor.service.ActorStatusService;
import org.example.tudubem.world.WorldUtils;
import org.example.tudubem.world.service.WorldService;
import org.example.tudubem.world.service.grid.GridMap;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Profile("monitor")
public class MonitorService {

    private final ActorStatusService actorStatusService;
    private final WorldService worldService;

    private final ConcurrentMap<Long, ActorStatus> latestActors = new ConcurrentHashMap<>();
    private final AtomicReference<GridMap> latestGridMap = new AtomicReference<>();
    private final List<Disposable> subscriptions = new ArrayList<>();
    private int previousLineLength = 0;

    public MonitorService(ActorStatusService actorStatusService, WorldService worldService) {
        this.actorStatusService = actorStatusService;
        this.worldService = worldService;
    }

    @PostConstruct
    void init() {
        subscriptions.add(actorStatusService.asFlux().subscribe(status -> {
            latestActors.put(status.actorId(), status);
            renderConsole();
        }));
        subscriptions.add(worldService.gridMapFlux().subscribe(gridMap -> {
            latestGridMap.set(gridMap);
            renderConsole();
        }));
    }

    @PreDestroy
    void destroy() {
        for (Disposable subscription : subscriptions) {
            subscription.dispose();
        }
    }

    public ResponseEntity<byte[]> getTrajectoryImage(Long mapId, Long actorId) {
        GridMap gridMap = worldService.getCached(mapId)
                .orElseGet(() -> worldService.buildAndCache(mapId));
        return WorldUtils.toPngResponse(
                gridMap,
                actorStatusService.getTrail(actorId),
                actorStatusService.getCurrentPointOrNull(actorId)
        );
    }

    private synchronized void renderConsole() {
        StringBuilder line = new StringBuilder();
        line.append("monitor | time=").append(LocalDateTime.now());
        GridMap gridMap = latestGridMap.get();
        if (gridMap == null) {
            line.append(" | world=none");
        } else {
            line.append(" | world=")
                    .append(gridMap.widthCells()).append("x").append(gridMap.heightCells())
                    .append("@").append(gridMap.cellSizePx());
        }

        if (latestActors.isEmpty()) {
            line.append(" | actors=empty");
        } else {
            line.append(" | actors=").append(latestActors.size());
            latestActors.values().stream()
                    .limit(3)
                    .forEach(status -> line.append(" [id=").append(status.actorId())
                            .append(",x=").append(status.x())
                            .append(",y=").append(status.y())
                            .append(",s=").append(status.speech() == null ? "" : status.speech())
                            .append("]"));
        }

        int currentLength = line.length();
        int padLength = Math.max(0, previousLineLength - currentLength);
        previousLineLength = currentLength;

        System.out.print("\r" + line + " ".repeat(padLength));
        System.out.flush();
    }
}
