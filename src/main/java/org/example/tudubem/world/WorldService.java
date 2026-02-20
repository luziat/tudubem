package org.example.tudubem.world;

import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.map.MapEntity;
import org.example.tudubem.world.map.MapService;
import org.example.tudubem.world.grid.GridMap;
import org.example.tudubem.world.grid.GridMapFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorldService {

    private final MapService mapService;
    private Long cachedMapId;
    private GridMap cachedGridMap;

    @Value("${app.grid.cell-size-px:8}")
    private int defaultCellSizePx;

    public GridMap buildAndCache(Long mapId) {
        return buildAndCache(mapId, defaultCellSizePx);
    }

    public GridMap buildAndCache(Long mapId, int cellSizePx) {
        MapEntity mapEntity = mapService.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("map not found: " + mapId));

        String sensorMapImagePath = mapEntity.getSensorMapImagePath();
        if (sensorMapImagePath == null || sensorMapImagePath.isBlank()) {
            throw new IllegalStateException("sensor map image path is empty for mapId=" + mapId);
        }

        GridMap gridMap = GridMapFactory.create(Path.of(sensorMapImagePath), cellSizePx);
        cachedMapId = mapId;
        cachedGridMap = gridMap;
        return gridMap;
    }

    public Optional<GridMap> getCached(Long mapId) {
        if (cachedMapId == null || !cachedMapId.equals(mapId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(cachedGridMap);
    }

    public void evict(Long mapId) {
        if (cachedMapId != null && cachedMapId.equals(mapId)) {
            cachedMapId = null;
            cachedGridMap = null;
        }
    }
}
