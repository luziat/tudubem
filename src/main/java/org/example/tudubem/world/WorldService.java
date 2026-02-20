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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WorldService {

    private final MapService mapService;
    private final KeepoutZoneService keepoutZoneService;

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

        GridMap baseGridMap = GridMapFactory.create(Path.of(sensorMapImagePath), cellSizePx);
        GridMap completedGridMap = applyEnabledKeepoutZones(mapId, baseGridMap);
        cachedMapId = mapId;
        cachedGridMap = completedGridMap;
        return completedGridMap;
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

    private GridMap applyEnabledKeepoutZones(Long mapId, GridMap baseGridMap) {
        List<KeepoutZoneEntity> keepoutZones = keepoutZoneService.findEnabledByMapId(mapId);
        if (keepoutZones.isEmpty()) {
            return baseGridMap;
        }

        List<List<Integer>> merged = deepCopy(baseGridMap.occupancy());
        for (KeepoutZoneEntity zone : keepoutZones) {
            List<Point2D.Double> polygonInPixels = parseVertices(zone.getVerticesJson());
            List<Point2D.Double> polygonInGrid = toGridScale(polygonInPixels, baseGridMap.cellSizePx());
            overlayPolygonAsOccupied(merged, polygonInGrid);
        }

        return new GridMap(
                baseGridMap.widthCells(),
                baseGridMap.heightCells(),
                baseGridMap.cellSizePx(),
                merged
        );
    }

    private List<List<Integer>> deepCopy(List<List<Integer>> source) {
        List<List<Integer>> copied = new ArrayList<>(source.size());
        for (List<Integer> row : source) {
            copied.add(new ArrayList<>(row));
        }
        return copied;
    }

    private List<Point2D.Double> parseVertices(String verticesJson) {
        if (verticesJson == null || verticesJson.isBlank()) {
            throw new IllegalStateException("keepout vertices_json is empty");
        }
        Pattern numberPattern = Pattern.compile("-?\\d+(?:\\.\\d+)?");
        Matcher matcher = numberPattern.matcher(verticesJson);

        List<Double> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        if (numbers.size() < 6 || numbers.size() % 2 != 0) {
            throw new IllegalStateException("invalid keepout vertices_json: " + verticesJson);
        }

        List<Point2D.Double> polygon = new ArrayList<>(numbers.size() / 2);
        for (int i = 0; i < numbers.size(); i += 2) {
            polygon.add(new Point2D.Double(numbers.get(i), numbers.get(i + 1)));
        }
        return polygon;
    }

    private List<Point2D.Double> toGridScale(List<Point2D.Double> polygonInPixels, int cellSizePx) {
        List<Point2D.Double> scaled = new ArrayList<>(polygonInPixels.size());
        for (Point2D.Double point : polygonInPixels) {
            scaled.add(new Point2D.Double(point.x / cellSizePx, point.y / cellSizePx));
        }
        return scaled;
    }

    private void overlayPolygonAsOccupied(List<List<Integer>> occupancy, List<Point2D.Double> polygonInGrid) {
        if (polygonInGrid.size() < 3) {
            return;
        }

        int height = occupancy.size();
        int width = occupancy.getFirst().size();

        for (int y = 0; y < height; y++) {
            double centerY = y + 0.5;
            List<Integer> row = occupancy.get(y);
            for (int x = 0; x < width; x++) {
                double centerX = x + 0.5;
                if (isPointInPolygon(centerX, centerY, polygonInGrid)) {
                    row.set(x, 1);
                }
            }
        }
    }

    private boolean isPointInPolygon(double x, double y, List<Point2D.Double> polygon) {
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            double xi = polygon.get(i).x;
            double yi = polygon.get(i).y;
            double xj = polygon.get(j).x;
            double yj = polygon.get(j).y;

            boolean intersect = ((yi > y) != (yj > y))
                    && (x < (xj - xi) * (y - yi) / ((yj - yi) + 1e-12) + xi);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }
}
