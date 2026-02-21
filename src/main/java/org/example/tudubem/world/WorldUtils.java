package org.example.tudubem.world;

import org.example.tudubem.world.grid.GridMap;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorldUtils {
    private WorldUtils() {
    }

    public static List<List<Integer>> deepCopy(List<List<Integer>> source) {
        List<List<Integer>> copied = new ArrayList<>(source.size());
        for (List<Integer> row : source) {
            copied.add(new ArrayList<>(row));
        }
        return copied;
    }

    public static List<List<Integer>> createEmptyLayer(int widthCells, int heightCells) {
        List<List<Integer>> layer = new ArrayList<>(heightCells);
        for (int y = 0; y < heightCells; y++) {
            List<Integer> row = new ArrayList<>(widthCells);
            for (int x = 0; x < widthCells; x++) {
                row.add(0);
            }
            layer.add(row);
        }
        return layer;
    }

    public static List<List<Integer>> combineLayers(
            List<List<Integer>> baseLayer,
            List<List<Integer>> keepoutLayer,
            List<List<Integer>> dynamicLayer
    ) {
        int height = baseLayer.size();
        int width = baseLayer.getFirst().size();
        List<List<Integer>> composite = new ArrayList<>(height);

        for (int y = 0; y < height; y++) {
            List<Integer> row = new ArrayList<>(width);
            for (int x = 0; x < width; x++) {
                int occupied = (baseLayer.get(y).get(x) == 1
                        || keepoutLayer.get(y).get(x) == 1
                        || dynamicLayer.get(y).get(x) == 1) ? 1 : 0;
                row.add(occupied);
            }
            composite.add(row);
        }
        return composite;
    }

    public static GridMap toGridMap(List<List<Integer>> layer, WorldBundle bundle) {
        return new GridMap(
                bundle.widthCells,
                bundle.heightCells,
                bundle.cellSizePx,
                deepCopy(layer)
        );
    }

    public static List<Point2D.Double> parseVertices(String verticesJson) {
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

    public static List<Point2D.Double> toGridScale(List<Point2D.Double> polygonInPixels, int cellSizePx) {
        List<Point2D.Double> scaled = new ArrayList<>(polygonInPixels.size());
        for (Point2D.Double point : polygonInPixels) {
            scaled.add(new Point2D.Double(point.x / cellSizePx, point.y / cellSizePx));
        }
        return scaled;
    }

    public static void overlayPolygonAsOccupied(List<List<Integer>> occupancy, List<Point2D.Double> polygonInGrid) {
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

    private static boolean isPointInPolygon(double x, double y, List<Point2D.Double> polygon) {
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
