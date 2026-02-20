package org.example.tudubem.area;

import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.WorldEntity;
import org.example.tudubem.world.WorldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    private final WorldRepository worldRepository;

    public List<AreaEntity> findAllByWorldId(Long worldId) {
        return areaRepository.findByWorld_Id(worldId);
    }

    public Optional<AreaEntity> findById(Long worldId, Long id) {
        return areaRepository.findByIdAndWorld_Id(id, worldId);
    }

    @Transactional
    public Optional<AreaEntity> create(Long worldId, AreaEntity request) {
        return worldRepository.findById(worldId)
                .map(world -> {
                    AreaEntity area = new AreaEntity();
                    area.setWorld(world);
                    apply(area, request);
                    return areaRepository.save(area);
                });
    }

    @Transactional
    public Optional<AreaEntity> update(Long worldId, Long id, AreaEntity request) {
        Optional<WorldEntity> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isEmpty()) {
            return Optional.empty();
        }

        return areaRepository.findByIdAndWorld_Id(id, worldId)
                .map(area -> {
                    area.setWorld(worldOptional.get());
                    apply(area, request);
                    return areaRepository.save(area);
                });
    }

    @Transactional
    public boolean delete(Long worldId, Long id) {
        return areaRepository.findByIdAndWorld_Id(id, worldId)
                .map(area -> {
                    areaRepository.delete(area);
                    return true;
                })
                .orElse(false);
    }

    private void apply(AreaEntity area, AreaEntity request) {
        area.setName(request.getName());
        area.setType(request.getType());
        area.setVerticesJson(request.getVerticesJson());
        area.setMetadataJson(request.getMetadataJson());
    }
}
