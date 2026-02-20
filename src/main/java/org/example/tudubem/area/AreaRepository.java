package org.example.tudubem.area;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<AreaEntity, Long> {
    List<AreaEntity> findByWorld_Id(Long worldId);

    Optional<AreaEntity> findByIdAndWorld_Id(Long id, Long worldId);
}
