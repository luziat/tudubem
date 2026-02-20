package org.example.tudubem.keepout;

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
public class KeepoutZoneService {

    private final KeepoutZoneRepository keepoutZoneRepository;
    private final WorldRepository worldRepository;

    public List<KeepoutZoneEntity> findAllByWorldId(Long worldId) {
        return keepoutZoneRepository.findByWorld_Id(worldId);
    }

    public Optional<KeepoutZoneEntity> findById(Long worldId, Long id) {
        return keepoutZoneRepository.findByIdAndWorld_Id(id, worldId);
    }

    @Transactional
    public Optional<KeepoutZoneEntity> create(Long worldId, KeepoutZoneEntity request) {
        return worldRepository.findById(worldId)
                .map(world -> {
                    KeepoutZoneEntity keepoutZone = new KeepoutZoneEntity();
                    keepoutZone.setWorld(world);
                    apply(keepoutZone, request);
                    return keepoutZoneRepository.save(keepoutZone);
                });
    }

    @Transactional
    public Optional<KeepoutZoneEntity> update(Long worldId, Long id, KeepoutZoneEntity request) {
        Optional<WorldEntity> worldOptional = worldRepository.findById(worldId);
        if (worldOptional.isEmpty()) {
            return Optional.empty();
        }

        return keepoutZoneRepository.findByIdAndWorld_Id(id, worldId)
                .map(keepoutZone -> {
                    keepoutZone.setWorld(worldOptional.get());
                    apply(keepoutZone, request);
                    return keepoutZoneRepository.save(keepoutZone);
                });
    }

    @Transactional
    public boolean delete(Long worldId, Long id) {
        return keepoutZoneRepository.findByIdAndWorld_Id(id, worldId)
                .map(keepoutZone -> {
                    keepoutZoneRepository.delete(keepoutZone);
                    return true;
                })
                .orElse(false);
    }

    private void apply(KeepoutZoneEntity keepoutZone, KeepoutZoneEntity request) {
        keepoutZone.setName(request.getName());
        keepoutZone.setVerticesJson(request.getVerticesJson());
        keepoutZone.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        keepoutZone.setReason(request.getReason());
    }
}
