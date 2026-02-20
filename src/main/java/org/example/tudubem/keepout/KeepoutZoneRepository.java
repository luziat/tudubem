package org.example.tudubem.keepout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeepoutZoneRepository extends JpaRepository<KeepoutZoneEntity, Long> {
    List<KeepoutZoneEntity> findByWorld_Id(Long worldId);

    Optional<KeepoutZoneEntity> findByIdAndWorld_Id(Long id, Long worldId);
}
