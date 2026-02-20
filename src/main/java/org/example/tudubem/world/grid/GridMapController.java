package org.example.tudubem.world.grid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grid-map")
@RequiredArgsConstructor
public class GridMapController {

    private final GridMapService gridMapService;

    @PostMapping("/{mapId}/build")
    public ResponseEntity<GridMap> buildAndCache(@PathVariable Long mapId) {
        try {
            GridMap gridMap = gridMapService.buildAndCache(mapId);
            return ResponseEntity.ok(gridMap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
