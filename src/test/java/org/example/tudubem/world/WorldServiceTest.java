package org.example.tudubem.world;

import org.example.tudubem.world.grid.GridMap;
import org.example.tudubem.world.keepout.KeepoutZoneEntity;
import org.example.tudubem.world.keepout.KeepoutZoneService;
import org.example.tudubem.world.map.MapEntity;
import org.example.tudubem.world.map.MapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldServiceTest {

    @Mock
    private MapService mapService;

    @Mock
    private KeepoutZoneService keepoutZoneService;

    @InjectMocks
    private WorldService worldService;

    @Test
    void buildAndCache_appliesEnabledKeepoutAreaAsOccupied() throws IOException {
        Path sensorMapPath = Path.of("src/test/hospital_refined_map.png");

        MapEntity mapEntity = new MapEntity();
        mapEntity.setId(1L);
        mapEntity.setSensorMapImagePath(sensorMapPath.toString());

        KeepoutZoneEntity keepoutZone = new KeepoutZoneEntity();
        keepoutZone.setMap(mapEntity);
        keepoutZone.setEnabled(true);
        keepoutZone.setVerticesJson("[[0,0],[1000,0],[1000,300],[0,300]]");

        when(mapService.findById(1L)).thenReturn(Optional.of(mapEntity));
        when(keepoutZoneService.findEnabledByMapId(1L)).thenReturn(List.of(keepoutZone));

        GridMap gridMap = worldService.buildAndCache(1L, 4);

        assertTrue(gridMap.widthCells() > 0);
        assertTrue(gridMap.heightCells() > 0);
        assertTrue(gridMap.occupancy().stream().flatMap(List::stream).anyMatch(v -> v == 1));
        assertTrue(worldService.getCached(1L).isPresent());

        Path outputDir = Path.of("build/reports/gridmap");
        Files.createDirectories(outputDir);
        Path visualizationPath = outputDir.resolve("world_service_completed_gridmap.png");
        writeGridVisualization(gridMap, visualizationPath);
        assertTrue(Files.exists(visualizationPath));
        assertTrue(Files.size(visualizationPath) > 0);

        Path summaryPath = outputDir.resolve("world_service_completed_gridmap_summary.txt");
        String summary = "widthCells=" + gridMap.widthCells()
                + ", heightCells=" + gridMap.heightCells()
                + ", cellSizePx=" + gridMap.cellSizePx();
        Files.writeString(summaryPath, summary, StandardCharsets.UTF_8);
        assertFalse(Files.readString(summaryPath, StandardCharsets.UTF_8).isBlank());
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
