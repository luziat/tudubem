package org.example.tudubem.space.grid;

import org.example.tudubem.world.grid.GridMap;
import org.example.tudubem.world.grid.GridMapFactory;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridMapFactoryTest {

    @Test
    void buildGridMapAndVisualize() throws IOException {
        Path sensorMapPath = Path.of("src/test/hospital_refined_map.png");
        int cellSizePx = 1;

        GridMapFactory factory = new GridMapFactory();
        GridMap gridMap = factory.create(sensorMapPath, cellSizePx);

        assertTrue(gridMap.widthCells() > 0, "widthCells must be greater than 0");
        assertTrue(gridMap.heightCells() > 0, "heightCells must be greater than 0");

        List<List<Integer>> occupancy = gridMap.occupancy();
        int occupiedCount = 0;
        int freeCount = 0;

        for (List<Integer> row : occupancy) {
            for (Integer value : row) {
                if (value == 1) {
                    occupiedCount++;
                } else {
                    freeCount++;
                }
            }
        }

        assertTrue(occupiedCount > 0, "occupied cells must exist");
        assertTrue(freeCount > 0, "free cells must exist");

        Path outputDir = Path.of("build/reports/gridmap");
        Files.createDirectories(outputDir);

        Path visualizationPath = outputDir.resolve("hospital_refined_grid_visualization.png");
        writeGridVisualization(gridMap, visualizationPath);
        assertTrue(Files.exists(visualizationPath), "visualization file must exist");
        assertTrue(Files.size(visualizationPath) > 0, "visualization file must not be empty");

        Path summaryPath = outputDir.resolve("hospital_refined_grid_summary.txt");
        String summary = "widthCells=" + gridMap.widthCells()
                + ", heightCells=" + gridMap.heightCells()
                + ", cellSizePx=" + gridMap.cellSizePx()
                + ", occupied=" + occupiedCount
                + ", free=" + freeCount;
        Files.writeString(summaryPath, summary);
        assertFalse(Files.readString(summaryPath).isBlank(), "summary text must not be blank");
    }

    private void writeGridVisualization(GridMap gridMap, Path outputPath) throws IOException {
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

        ImageIO.write(image, "png", outputPath.toFile());
    }
}
