package org.example.tudubem.world;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.keepout.KeepoutZoneEntity;
import org.example.tudubem.world.keepout.KeepoutZoneService;
import org.example.tudubem.world.map.MapEntity;
import org.example.tudubem.world.map.MapService;
import org.example.tudubem.world.grid.GridMap;
import org.example.tudubem.world.grid.GridMapFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorldService {

    private final MapService mapService;
    private final KeepoutZoneService keepoutZoneService;

    private WorldBundle cachedWorldBundle;

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

        GridMap baseGridMap = GridMapFactory.create(Path.of(sensorMapImagePath), cellSizePx);
        WorldBundle previous = cachedWorldBundle;

        List<List<Integer>> baseLayer = WorldUtils.deepCopy(baseGridMap.occupancy());
        List<List<Integer>> keepoutLayer = buildKeepoutLayer(mapId, baseGridMap);
        List<List<Integer>> dynamicLayer = WorldUtils.createEmptyLayer(baseGridMap.widthCells(), baseGridMap.heightCells());

        Map<String, List<Point2D.Double>> dynamicObjects = new HashMap<>();
        if (previous != null && previous.hasSameGridConfig(
                mapId,
                baseGridMap.widthCells(),
                baseGridMap.heightCells(),
                baseGridMap.cellSizePx()
        )) {
            dynamicObjects.putAll(previous.dynamicObjectsInGrid);
        }

        List<List<Integer>> compositeLayer = WorldUtils.combineLayers(baseLayer, keepoutLayer, dynamicLayer);
        cachedWorldBundle = new WorldBundle(
                mapId,
                baseGridMap.widthCells(),
                baseGridMap.heightCells(),
                baseGridMap.cellSizePx(),
                baseLayer,
                keepoutLayer,
                dynamicLayer,
                compositeLayer,
                dynamicObjects
        );
        rebuildDynamicAndComposite(cachedWorldBundle);
        return WorldUtils.toGridMap(cachedWorldBundle.composite, cachedWorldBundle);
    }

    public Optional<GridMap> getCached(Long mapId) {
        if (cachedWorldBundle == null || !cachedWorldBundle.mapId.equals(mapId)) {
            return Optional.empty();
        }
        return Optional.of(WorldUtils.toGridMap(cachedWorldBundle.composite, cachedWorldBundle));
    }

    public void evict(Long mapId) {
        if (cachedWorldBundle != null && cachedWorldBundle.mapId.equals(mapId)) {
            cachedWorldBundle = null;
        }
    }

    public GridMap upsertDynamicObject(Long mapId, String objectId, String verticesJson) {
        WorldBundle bundle = ensureWorldBundle(mapId);
        List<Point2D.Double> polygonInPixels = WorldUtils.parseVertices(verticesJson);
        List<Point2D.Double> polygonInGrid = WorldUtils.toGridScale(polygonInPixels, bundle.cellSizePx);
        bundle.dynamicObjectsInGrid.put(objectId, polygonInGrid);
        rebuildDynamicAndComposite(bundle);
        return WorldUtils.toGridMap(bundle.composite, bundle);
    }

    public GridMap removeDynamicObject(Long mapId, String objectId) {
        WorldBundle bundle = ensureWorldBundle(mapId);
        bundle.dynamicObjectsInGrid.remove(objectId);
        rebuildDynamicAndComposite(bundle);
        return WorldUtils.toGridMap(bundle.composite, bundle);
    }

    public GridMap clearDynamicObjects(Long mapId) {
        WorldBundle bundle = ensureWorldBundle(mapId);
        bundle.dynamicObjectsInGrid.clear();
        rebuildDynamicAndComposite(bundle);
        return WorldUtils.toGridMap(bundle.composite, bundle);
    }

    private WorldBundle ensureWorldBundle(Long mapId) {
        if (cachedWorldBundle == null || !cachedWorldBundle.mapId.equals(mapId)) {
            buildAndCache(mapId);
        }
        return cachedWorldBundle;
    }

    private List<List<Integer>> buildKeepoutLayer(Long mapId, GridMap baseGridMap) {
        List<List<Integer>> keepoutLayer = WorldUtils.createEmptyLayer(baseGridMap.widthCells(), baseGridMap.heightCells());
        List<KeepoutZoneEntity> keepoutZones = keepoutZoneService.findEnabledByMapId(mapId);
        for (KeepoutZoneEntity zone : keepoutZones) {
            List<Point2D.Double> polygonInPixels = WorldUtils.parseVertices(zone.getVerticesJson());
            List<Point2D.Double> polygonInGrid = WorldUtils.toGridScale(polygonInPixels, baseGridMap.cellSizePx());
            WorldUtils.overlayPolygonAsOccupied(keepoutLayer, polygonInGrid);
        }
        return keepoutLayer;
    }

    private void rebuildDynamicAndComposite(WorldBundle bundle) {
        List<List<Integer>> dynamicLayer = WorldUtils.createEmptyLayer(bundle.widthCells, bundle.heightCells);
        applyDynamicObjects(dynamicLayer, bundle.dynamicObjectsInGrid);
        bundle.dynamic = dynamicLayer;
        bundle.composite = WorldUtils.combineLayers(bundle.base, bundle.keepout, bundle.dynamic);
    }

    private void applyDynamicObjects(List<List<Integer>> dynamicLayer, Map<String, List<Point2D.Double>> dynamicObjects) {
        for (List<Point2D.Double> polygon : dynamicObjects.values()) {
            WorldUtils.overlayPolygonAsOccupied(dynamicLayer, polygon);
        }
    }

}
