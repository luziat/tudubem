package org.example.tudubem.actor.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.actor.dto.ActorStatus;
import org.example.tudubem.actor.entity.ActorEntity;
import org.example.tudubem.actor.service.pathfind.GridPoint;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class ActorStatusService {

    private static final int MAX_TRAIL_POINTS = 1_000;
    private static final int DEFAULT_START_X = 10;
    private static final int DEFAULT_START_Y = 10;
    private static final Sinks.EmitFailureHandler RETRY_NON_SERIALIZED =
            (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED;

    private final ActorRegistry actorRegistry;
    private final ConcurrentMap<Long, ActorStatus> statuses = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Deque<GridPoint>> trails = new ConcurrentHashMap<>();
    private final Sinks.Many<ActorStatus> statusSink = Sinks.many().replay().latest();

    @PostConstruct
    public void init() {
        actorRegistry.refresh();
        int offset = 0;
        for (ActorEntity actor : actorRegistry.findAllActive()) {
            if (actor.getId() == null) {
                continue;
            }
            int size = actor.getSize() == null || actor.getSize() < 1 ? 1 : actor.getSize();
            upsert(new ActorStatus(actor.getId(), size, DEFAULT_START_X + offset, DEFAULT_START_Y, ""));
            offset++;
        }
    }

    public Flux<ActorStatus> asFlux() {
        return statusSink.asFlux();
    }

    public Flux<ActorStatus> asFlux(Long actorId) {
        return asFlux()
                .filter(status -> status.actorId() != null && status.actorId().equals(actorId));
    }

    public Map<Long, ActorStatus> current() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(statuses));
    }

    public GridPoint getCurrentPoint(Long actorId) {
        ActorStatus actorStatus = statuses.get(actorId);
        return new GridPoint(actorStatus.x(), actorStatus.y());
    }

    public GridPoint getCurrentPointOrNull(Long actorId) {
        ActorStatus actorStatus = statuses.get(actorId);
        if (actorStatus == null) {
            return null;
        }
        return new GridPoint(actorStatus.x(), actorStatus.y());
    }

    public ActorStatus getCurrentStatusOrNull(Long actorId) {
        return statuses.get(actorId);
    }

    public java.util.List<GridPoint> getTrail(Long actorId) {
        Deque<GridPoint> trail = trails.get(actorId);
        if (trail == null) {
            return java.util.List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(trail));
    }

    public void upsert(ActorStatus status) {
        if (status == null || status.actorId() == null) {
            return;
        }
        ActorStatus sanitized = sanitize(status);
        statuses.put(sanitized.actorId(), sanitized);
        addTrailPoint(sanitized.actorId(), new GridPoint(sanitized.x(), sanitized.y()));
        statusSink.emitNext(sanitized, RETRY_NON_SERIALIZED);
    }

    public void remove(Long actorId) {
        if (actorId == null) {
            return;
        }
        statuses.remove(actorId);
        trails.remove(actorId);
    }

    public void clear() {
        statuses.clear();
        trails.clear();
    }

    private ActorStatus sanitize(ActorStatus status) {
        int size = status.size() < 1 ? 1 : status.size();
        return new ActorStatus(status.actorId(), size, status.x(), status.y(), status.speech());
    }

    private void addTrailPoint(Long actorId, GridPoint point) {
        Deque<GridPoint> trail = trails.computeIfAbsent(actorId, key -> new ConcurrentLinkedDeque<>());
        GridPoint last = trail.peekLast();
        if (last != null && last.x() == point.x() && last.y() == point.y()) {
            return;
        }
        trail.addLast(point);
        while (trail.size() > MAX_TRAIL_POINTS) {
            trail.pollFirst();
        }
    }
}
