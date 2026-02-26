package org.example.tudubem.world;

import org.example.tudubem.world.grid.GridMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorldUtils {
    private WorldUtils() {
    }

    // 2차원 점유 그리드를 깊은 복사해 원본 변경이 전파되지 않도록 한다.
    public static List<List<Integer>> deepCopy(List<List<Integer>> source) {
        List<List<Integer>> copied = new ArrayList<>(source.size());
        for (List<Integer> row : source) {
            copied.add(new ArrayList<>(row));
        }
        return copied;
    }

    // width x height 크기의 빈 레이어를 생성한다. 모든 셀은 0(비점유)으로 초기화된다.
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

    // base/keepout/dynamic 레이어를 OR 연산으로 합쳐 최종 점유(composite) 레이어를 만든다.
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

    // 내부 레이어 데이터를 GridMap 응답 객체로 변환한다.
    // 외부에서 GridMap을 수정해도 캐시 원본이 훼손되지 않도록 occupancy는 깊은 복사한다.
    public static GridMap toGridMap(List<List<Integer>> layer, WorldBundle bundle) {
        return new GridMap(
                bundle.widthCells,
                bundle.heightCells,
                bundle.cellSizePx,
                deepCopy(layer)
        );
    }

    // GridMap 점유 정보를 흑백 PNG로 인코딩해 HTTP 응답으로 반환한다.
    // 점유 셀(1)은 검정, 비점유 셀(0)은 흰색이며, y축은 이미지 좌표계에 맞춰 뒤집어 그린다.
    public static ResponseEntity<byte[]> toPngResponse(GridMap gridMap) {
        int width = gridMap.widthCells();
        int height = gridMap.heightCells();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            List<Integer> row = gridMap.occupancy().get(y);
            for (int x = 0; x < width; x++) {
                boolean occupied = row.get(x) == 1;
                int rgb = occupied ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
                image.setRGB(x, height - 1 - y, rgb);
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                    .body(baos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GridMap 위에 actor 이동 궤적(파랑)과 현재 위치(빨강)를 오버레이한 PNG 응답을 반환한다.
    public static ResponseEntity<byte[]> toPngResponse(
            GridMap gridMap,
            List<org.example.tudubem.actor.pathfind.GridPoint> trail,
            org.example.tudubem.actor.pathfind.GridPoint current
    ) {
        int width = gridMap.widthCells();
        int height = gridMap.heightCells();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            List<Integer> row = gridMap.occupancy().get(y);
            for (int x = 0; x < width; x++) {
                boolean occupied = row.get(x) == 1;
                int rgb = occupied ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
                image.setRGB(x, height - 1 - y, rgb);
            }
        }

        if (trail != null) {
            int trailRgb = new Color(0, 122, 255).getRGB();
            for (org.example.tudubem.actor.pathfind.GridPoint point : trail) {
                if (point == null) {
                    continue;
                }
                if (point.x() < 0 || point.y() < 0 || point.x() >= width || point.y() >= height) {
                    continue;
                }
                image.setRGB(point.x(), height - 1 - point.y(), trailRgb);
            }
        }

        if (current != null
                && current.x() >= 0 && current.y() >= 0
                && current.x() < width && current.y() < height) {
            int currentRgb = Color.RED.getRGB();
            image.setRGB(current.x(), height - 1 - current.y(), currentRgb);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                    .body(baos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // verticesJson 문자열에서 숫자만 추출해 (x, y) 좌표 목록으로 파싱한다.
    // 좌표는 최소 3개(숫자 6개) 이상이어야 폴리곤으로 인정한다.
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

    // 픽셀 좌표 폴리곤을 그리드 좌표계로 변환한다.
    // 각 좌표는 cellSizePx로 나누며, 소수점 값은 그대로 유지해 폴리곤 경계를 보존한다.
    public static List<Point2D.Double> toGridScale(List<Point2D.Double> polygonInPixels, int cellSizePx) {
        List<Point2D.Double> scaled = new ArrayList<>(polygonInPixels.size());
        for (Point2D.Double point : polygonInPixels) {
            scaled.add(new Point2D.Double(point.x / cellSizePx, point.y / cellSizePx));
        }
        return scaled;
    }

    // 폴리곤 내부에 포함되는 셀 중심점을 점유(1)로 마킹한다.
    // 폴리곤 점이 3개 미만이면 유효한 면적이 없다고 보고 반영하지 않는다.
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

    // 레이 캐스팅(ray casting) 방식으로 점(x, y)이 폴리곤 내부에 있는지 판별한다.
    // 경계 근처의 0 나눗셈 위험을 줄이기 위해 작은 epsilon을 더해 계산한다.
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
