package org.example.tudubem.world.keepout;

import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.map.MapEntity;
import org.example.tudubem.world.map.MapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KeepoutZoneService {

    private final KeepoutZoneRepository keepoutZoneRepository;
    private final MapRepository mapRepository;

    public List<KeepoutZoneEntity> findAllByMapId(Long mapId) {
        return keepoutZoneRepository.findByMap_Id(mapId);
    }

    public Optional<KeepoutZoneEntity> findById(Long mapId, Long id) {
        return keepoutZoneRepository.findByIdAndMap_Id(id, mapId);
    }

    @Transactional
    public Optional<KeepoutZoneEntity> create(Long mapId, KeepoutZoneEntity request) {
        return mapRepository.findById(mapId)
                .map(map -> {
                    KeepoutZoneEntity keepoutZone = new KeepoutZoneEntity();
                    keepoutZone.setMap(map);
                    apply(keepoutZone, request);
                    return keepoutZoneRepository.save(keepoutZone);
                });
    }

    @Transactional
    public Optional<KeepoutZoneEntity> update(Long mapId, Long id, KeepoutZoneEntity request) {
        Optional<MapEntity> mapOptional = mapRepository.findById(mapId);
        if (mapOptional.isEmpty()) {
            return Optional.empty();
        }

        return keepoutZoneRepository.findByIdAndMap_Id(id, mapId)
                .map(keepoutZone -> {
                    keepoutZone.setMap(mapOptional.get());
                    apply(keepoutZone, request);
                    return keepoutZoneRepository.save(keepoutZone);
                });
    }

    @Transactional
    public boolean delete(Long mapId, Long id) {
        return keepoutZoneRepository.findByIdAndMap_Id(id, mapId)
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
