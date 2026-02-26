package org.example.tudubem.actor.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActorRepository extends JpaRepository<ActorEntity, Long> {
    List<ActorEntity> findByEnabledTrue();
}
