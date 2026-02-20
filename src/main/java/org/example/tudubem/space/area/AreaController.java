package org.example.tudubem.space.area;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/map/{mapId}/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    public List<AreaEntity> findAll(@PathVariable Long mapId) {
        return areaService.findAllByMapId(mapId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaEntity> findById(@PathVariable Long mapId, @PathVariable Long id) {
        return areaService.findById(mapId, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AreaEntity> create(@PathVariable Long mapId, @RequestBody AreaEntity request) {
        return areaService.create(mapId, request)
                .map(saved -> ResponseEntity.created(URI.create("/map/" + mapId + "/areas/" + saved.getId())).body(saved))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaEntity> update(@PathVariable Long mapId, @PathVariable Long id, @RequestBody AreaEntity request) {
        return areaService.update(mapId, id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long mapId, @PathVariable Long id) {
        if (!areaService.delete(mapId, id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
