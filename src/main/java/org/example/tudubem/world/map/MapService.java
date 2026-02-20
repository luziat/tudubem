package org.example.tudubem.world.map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;

    public List<MapEntity> findAll() {
        return mapRepository.findAll();
    }

    public Optional<MapEntity> findById(Long id) {
        return mapRepository.findById(id);
    }

    @Transactional
    public MapEntity create(MapEntity mapEntity) {
        return mapRepository.save(mapEntity);
    }

    @Transactional
    public Optional<MapEntity> update(Long id, MapEntity request) {
        return mapRepository.findById(id)
                .map(mapEntity -> {
                    mapEntity.setName(request.getName());
                    return mapRepository.save(mapEntity);
                });
    }

    @Transactional
    public Optional<MapEntity> updateSensorMapPath(Long id, String sensorMapImagePath) {
        return mapRepository.findById(id)
                .map(mapEntity -> {
                    mapEntity.setSensorMapImagePath(sensorMapImagePath);
                    return mapRepository.save(mapEntity);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!mapRepository.existsById(id)) {
            return false;
        }
        mapRepository.deleteById(id);
        return true;
    }
}
