package org.example.tudubem.world.service;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.dto.WorldBundle;
import org.example.tudubem.world.WorldUtils;
import org.example.tudubem.world.entity.KeepoutZoneEntity;
import org.example.tudubem.world.entity.MapEntity;
import org.example.tudubem.world.service.grid.GridMap;
import org.example.tudubem.world.service.grid.GridMapFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorldService extends WorldDataStore {

    private final MapService mapService;
    private final KeepoutZoneService keepoutZoneService;

    @Value("${app.grid.cell-size-px:1}")
    private int defaultCellSizePx;

    // 기본 셀 크기로 그리드맵을 생성하고 캐시한다.
    public GridMap buildAndCache(Long mapId) {
        return buildAndCache(mapId, defaultCellSizePx);
    }

    // 지정한 셀 크기로 그리드맵을 생성하고 캐시한다.
    public GridMap buildAndCache(Long mapId, int cellSizePx) {
        MapEntity mapEntity = mapService.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("map not found: " + mapId));

        String sensorMapImagePath = mapEntity.getSensorMapImagePath();
        if (sensorMapImagePath == null || sensorMapImagePath.isBlank()) {
            throw new IllegalStateException("sensor map image path is empty for mapId=" + mapId);
        }

        GridMap baseGridMap = GridMapFactory.create(Path.of(sensorMapImagePath), cellSizePx);
        WorldBundle previous = current().orElse(null);

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
        WorldBundle newBundle = new WorldBundle(
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
        rebuildDynamicAndComposite(newBundle);
        publish(newBundle);
        return WorldUtils.toGridMap(newBundle.composite, newBundle);
    }

    // 캐시된 그리드맵을 조회한다.
    public Optional<GridMap> getCached(Long mapId) {
        Optional<WorldBundle> cached = current();
        if (cached.isEmpty() || !cached.get().mapId.equals(mapId)) {
            return Optional.empty();
        }
        WorldBundle bundle = cached.get();
        return Optional.of(WorldUtils.toGridMap(bundle.composite, bundle));
    }

    // 특정 mapId의 월드 번들 변경만 필터링해 GridMap 스트림으로 노출한다.
    public Flux<GridMap> asFlux(Long mapId) {
        return asFlux()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(bundle -> bundle.mapId.equals(mapId))
                .map(bundle -> WorldUtils.toGridMap(bundle.composite, bundle));
    }

    // 모든 mapId의 월드 번들 변경을 GridMap 스트림으로 노출한다.
    public Flux<GridMap> gridMapFlux() {
        return asFlux()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(bundle -> WorldUtils.toGridMap(bundle.composite, bundle));
    }

    // 캐시된 그리드맵을 제거한다.
    public void evict(Long mapId) {
        Optional<WorldBundle> cached = current();
        if (cached.isPresent() && cached.get().mapId.equals(mapId)) {
            clear();
        }
    }

    // 동적 객체를 추가 또는 갱신한다.
    public GridMap upsertDynamicObject(Long mapId, String objectId, String verticesJson) {
        WorldBundle bundle = ensureWorldBundle(mapId);
        List<Point2D.Double> polygonInPixels = WorldUtils.parseVertices(verticesJson);
        List<Point2D.Double> polygonInGrid = WorldUtils.toGridScale(polygonInPixels, bundle.cellSizePx);
        bundle.dynamicObjectsInGrid.put(objectId, polygonInGrid);
        rebuildDynamicAndComposite(bundle);
        publish(bundle);
        return WorldUtils.toGridMap(bundle.composite, bundle);
    }

    // 동적 객체를 제거한다.
    public GridMap removeDynamicObject(Long mapId, String objectId) {
        WorldBundle bundle = ensureWorldBundle(mapId);
        bundle.dynamicObjectsInGrid.remove(objectId);
        rebuildDynamicAndComposite(bundle);
        publish(bundle);
        return WorldUtils.toGridMap(bundle.composite, bundle);
    }

    // 동적 객체를 모두 제거한다.
    public GridMap clearDynamicObjects(Long mapId) {
        WorldBundle bundle = ensureWorldBundle(mapId);
        bundle.dynamicObjectsInGrid.clear();
        rebuildDynamicAndComposite(bundle);
        publish(bundle);
        return WorldUtils.toGridMap(bundle.composite, bundle);
    }

    // 요청한 mapId의 캐시가 없으면 새로 생성한다.
    private WorldBundle ensureWorldBundle(Long mapId) {
        Optional<WorldBundle> cached = current();
        if (cached.isEmpty() || !cached.get().mapId.equals(mapId)) {
            buildAndCache(mapId);
        }
        return current()
                .orElseThrow(() -> new IllegalStateException("world bundle cache is empty"));
    }

    // 활성화된 keepout 영역을 레이어로 변환한다.
    private List<List<Integer>> buildKeepoutLayer(Long mapId, GridMap baseGridMap) {
        // keepout 레이어 초기화
        List<List<Integer>> keepoutLayer = WorldUtils.createEmptyLayer(baseGridMap.widthCells(), baseGridMap.heightCells());

        // keepout 계산
        List<KeepoutZoneEntity> keepoutZones = keepoutZoneService.findEnabledByMapId(mapId);
        for (KeepoutZoneEntity zone : keepoutZones) {
            List<Point2D.Double> polygonInPixels = WorldUtils.parseVertices(zone.getVerticesJson());
            List<Point2D.Double> polygonInGrid = WorldUtils.toGridScale(polygonInPixels, baseGridMap.cellSizePx());
            WorldUtils.overlayPolygonAsOccupied(keepoutLayer, polygonInGrid);
        }
        return keepoutLayer;
    }

    // 동적 레이어와 최종 합성 레이어를 다시 계산한다.
    private void rebuildDynamicAndComposite(WorldBundle bundle) {

        // 다이나믹 레이어 초기화
        List<List<Integer>> dynamicLayer = WorldUtils.createEmptyLayer(bundle.widthCells, bundle.heightCells);

        // 다이나믹 객체 재계산
        for (List<Point2D.Double> polygon : bundle.dynamicObjectsInGrid.values()) {
            WorldUtils.overlayPolygonAsOccupied(dynamicLayer, polygon);
        }

        bundle.dynamic = dynamicLayer;
        bundle.composite = WorldUtils.combineLayers(bundle.base, bundle.keepout, bundle.dynamic);
    }

}
