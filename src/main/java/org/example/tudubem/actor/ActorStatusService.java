package org.example.tudubem.actor;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ActorStatusService {

    private static final Sinks.EmitFailureHandler RETRY_NON_SERIALIZED =
            (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED;

    private final ConcurrentMap<Long, ActorStatus> statuses = new ConcurrentHashMap<>();
    private final Sinks.Many<ActorStatus> statusSink = Sinks.many().replay().latest();

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

    public void upsert(ActorStatus status) {
        if (status == null || status.actorId() == null) {
            return;
        }
        ActorStatus sanitized = sanitize(status);
        statuses.put(sanitized.actorId(), sanitized);
        statusSink.emitNext(sanitized, RETRY_NON_SERIALIZED);
    }

    public void remove(Long actorId) {
        if (actorId == null) {
            return;
        }
        statuses.remove(actorId);
    }

    public void clear() {
        statuses.clear();
    }

    private ActorStatus sanitize(ActorStatus status) {
        return new ActorStatus(status.actorId(), status.x(), status.y(), status.speech());
    }
}
