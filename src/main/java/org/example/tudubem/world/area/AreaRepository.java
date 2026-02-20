package org.example.tudubem.world.area;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<AreaEntity, Long> {
    List<AreaEntity> findByMap_Id(Long mapId);

    Optional<AreaEntity> findByIdAndMap_Id(Long id, Long mapId);
}
