package org.example.tudubem.world.service;

import org.example.tudubem.world.dto.WorldBundle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class WorldDataStore {

    private final Sinks.Many<Optional<WorldBundle>> worldBundleSink = Sinks.many().replay().latest();
    private final AtomicReference<Optional<WorldBundle>> worldBundleRef = new AtomicReference<>(Optional.empty());

    @PostConstruct
    void init() {
        worldBundleSink.asFlux().subscribe(worldBundleRef::set);
        worldBundleSink.emitNext(Optional.empty(), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    protected Optional<WorldBundle> current() {
        return worldBundleRef.get();
    }

    protected Flux<Optional<WorldBundle>> asFlux() {
        return worldBundleSink.asFlux();
    }

    protected void publish(WorldBundle worldBundle) {
        worldBundleSink.emitNext(Optional.of(worldBundle), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    protected void clear() {
        worldBundleSink.emitNext(Optional.empty(), Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
