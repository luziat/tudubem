package org.example.tudubem.space.keepout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeepoutZoneRepository extends JpaRepository<KeepoutZoneEntity, Long> {
    List<KeepoutZoneEntity> findByMap_Id(Long mapId);

    Optional<KeepoutZoneEntity> findByIdAndMap_Id(Long id, Long mapId);
}
