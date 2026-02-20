package org.example.tudubem.space.keepout;

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
@RequestMapping("/map/{mapId}/keepout-zones")
@RequiredArgsConstructor
public class KeepoutZoneController {

    private final KeepoutZoneService keepoutZoneService;

    @GetMapping
    public List<KeepoutZoneEntity> findAll(@PathVariable Long mapId) {
        return keepoutZoneService.findAllByMapId(mapId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeepoutZoneEntity> findById(@PathVariable Long mapId, @PathVariable Long id) {
        return keepoutZoneService.findById(mapId, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KeepoutZoneEntity> create(@PathVariable Long mapId, @RequestBody KeepoutZoneEntity request) {
        return keepoutZoneService.create(mapId, request)
                .map(saved -> ResponseEntity.created(URI.create("/map/" + mapId + "/keepout-zones/" + saved.getId())).body(saved))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<KeepoutZoneEntity> update(@PathVariable Long mapId, @PathVariable Long id, @RequestBody KeepoutZoneEntity request) {
        return keepoutZoneService.update(mapId, id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long mapId, @PathVariable Long id) {
        if (!keepoutZoneService.delete(mapId, id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
