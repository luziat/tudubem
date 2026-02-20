package org.example.tudubem.robot;

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
@RequestMapping("/robot")
@RequiredArgsConstructor
public class RobotController {

    private final RobotService robotService;

    @GetMapping
    public List<RobotEntity> findAll() {
        return robotService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RobotEntity> findById(@PathVariable Long id) {
        return robotService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RobotEntity> create(@RequestBody RobotEntity robotEntity) {
        RobotEntity saved = robotService.create(robotEntity);
        return ResponseEntity.created(URI.create("/robot/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RobotEntity> update(@PathVariable Long id, @RequestBody RobotEntity request) {
        return robotService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!robotService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
