package org.example.tudubem.world.service.grid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GridMapFactory {

    public static GridMap create(Path sensorMapImagePath, int cellSizePx) {
        return create(sensorMapImagePath, cellSizePx, 127);
    }

    public static GridMap create(Path sensorMapImagePath, int cellSizePx, int occupiedThresholdGray) {
        if (cellSizePx <= 0) {
            throw new IllegalArgumentException("cellSizePx must be greater than 0");
        }
        if (occupiedThresholdGray < 0 || occupiedThresholdGray > 255) {
            throw new IllegalArgumentException("occupiedThresholdGray must be between 0 and 255");
        }
        if (sensorMapImagePath == null || !Files.exists(sensorMapImagePath)) {
            throw new IllegalArgumentException("sensor map image does not exist: " + sensorMapImagePath);
        }

        BufferedImage image;
        try {
            image = ImageIO.read(sensorMapImagePath.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("failed to read sensor map image: " + sensorMapImagePath, e);
        }
        if (image == null) {
            throw new IllegalStateException("unsupported image format: " + sensorMapImagePath);
        }

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int widthCells = (int) Math.ceil((double) imageWidth / cellSizePx);
        int heightCells = (int) Math.ceil((double) imageHeight / cellSizePx);

        List<List<Integer>> occupancy = new ArrayList<>(heightCells);
        for (int gridY = 0; gridY < heightCells; gridY++) {
            List<Integer> row = new ArrayList<>(widthCells);
            for (int gridX = 0; gridX < widthCells; gridX++) {
                row.add(isOccupiedCell(image, gridX, gridY, cellSizePx, occupiedThresholdGray) ? 1 : 0);
            }
            occupancy.add(row);
        }

        return new GridMap(widthCells, heightCells, cellSizePx, occupancy);
    }

    private static boolean isOccupiedCell(
            BufferedImage image,
            int gridX,
            int gridY,
            int cellSizePx,
            int occupiedThresholdGray
    ) {
        int startX = gridX * cellSizePx;
        int startY = gridY * cellSizePx;
        int endX = Math.min(startX + cellSizePx, image.getWidth());
        int endY = Math.min(startY + cellSizePx, image.getHeight());

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int rgb = image.getRGB(x, image.getHeight() - 1 - y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;
                if (gray <= occupiedThresholdGray) {
                    return true;
                }
            }
        }
        return false;
    }
}
