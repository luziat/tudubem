package org.example.tudubem.keepout;

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
@RequestMapping("/world/{worldId}/keepout-zones")
@RequiredArgsConstructor
public class KeepoutZoneController {

    private final KeepoutZoneService keepoutZoneService;

    @GetMapping
    public List<KeepoutZoneEntity> findAll(@PathVariable Long worldId) {
        return keepoutZoneService.findAllByWorldId(worldId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeepoutZoneEntity> findById(@PathVariable Long worldId, @PathVariable Long id) {
        return keepoutZoneService.findById(worldId, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KeepoutZoneEntity> create(@PathVariable Long worldId, @RequestBody KeepoutZoneEntity request) {
        return keepoutZoneService.create(worldId, request)
                .map(saved -> ResponseEntity.created(URI.create("/world/" + worldId + "/keepout-zones/" + saved.getId())).body(saved))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<KeepoutZoneEntity> update(@PathVariable Long worldId, @PathVariable Long id, @RequestBody KeepoutZoneEntity request) {
        return keepoutZoneService.update(worldId, id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long worldId, @PathVariable Long id) {
        if (!keepoutZoneService.delete(worldId, id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
