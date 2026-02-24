package org.example.tudubem.actor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class ActorRegistry {

    private final ActorRepository actorRepository;
    private final ConcurrentMap<Long, ActorEntity> activeActors = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        refresh();
    }

    public void refresh() {
        activeActors.clear();
        for (ActorEntity actor : actorRepository.findByEnabledTrue()) {
            if (actor.getId() != null) {
                activeActors.put(actor.getId(), actor);
            }
        }
    }

}
