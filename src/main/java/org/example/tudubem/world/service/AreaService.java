package org.example.tudubem.world.service;

import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.entity.AreaEntity;
import org.example.tudubem.world.entity.AreaRepository;
import org.example.tudubem.world.entity.MapEntity;
import org.example.tudubem.world.entity.MapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    private final MapRepository mapRepository;

    public List<AreaEntity> findAllByMapId(Long mapId) {
        return areaRepository.findByMap_Id(mapId);
    }

    public Optional<AreaEntity> findById(Long mapId, Long id) {
        return areaRepository.findByIdAndMap_Id(id, mapId);
    }

    @Transactional
    public Optional<AreaEntity> create(Long mapId, AreaEntity request) {
        return mapRepository.findById(mapId)
                .map(map -> {
                    AreaEntity area = new AreaEntity();
                    area.setMap(map);
                    apply(area, request);
                    return areaRepository.save(area);
                });
    }

    @Transactional
    public Optional<AreaEntity> update(Long mapId, Long id, AreaEntity request) {
        Optional<MapEntity> mapOptional = mapRepository.findById(mapId);
        if (mapOptional.isEmpty()) {
            return Optional.empty();
        }

        return areaRepository.findByIdAndMap_Id(id, mapId)
                .map(area -> {
                    area.setMap(mapOptional.get());
                    apply(area, request);
                    return areaRepository.save(area);
                });
    }

    @Transactional
    public boolean delete(Long mapId, Long id) {
        return areaRepository.findByIdAndMap_Id(id, mapId)
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
