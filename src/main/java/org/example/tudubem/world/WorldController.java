package org.example.tudubem.world;

import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.grid.GridMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/grid-map")
@RequiredArgsConstructor
public class WorldController {

    private final WorldService worldService;

    @PostMapping("/{mapId}/build")
    public ResponseEntity<GridMap> buildAndCache(@PathVariable Long mapId) {
        try {
            GridMap gridMap = worldService.buildAndCache(mapId);
            return ResponseEntity.ok(gridMap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/{mapId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getCachedGridMapImage(@PathVariable Long mapId) {
        return worldService.getCached(mapId)
                .map(this::toPngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<byte[]> toPngResponse(GridMap gridMap) {
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
}
