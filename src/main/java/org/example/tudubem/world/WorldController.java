package org.example.tudubem.world;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/world")
@RequiredArgsConstructor
public class WorldController {
    private final WorldService worldService;
    @Value("${app.world.image-dir:./data/world}")
    private String worldImageDir;

    @GetMapping
    public List<WorldEntity> findAll() {
        return worldService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorldEntity> findById(@PathVariable Long id) {
        return worldService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/sensor-map")
    public ResponseEntity<Resource> getSensorMap(@PathVariable Long id) {
        return worldService.findById(id)
                .flatMap(worldEntity -> {
                    String sensorMapImagePath = worldEntity.getSensorMapImagePath();
                    if (sensorMapImagePath == null || sensorMapImagePath.isBlank()) {
                        return java.util.Optional.empty();
                    }
                    Path path = Paths.get(sensorMapImagePath).normalize();
                    if (!Files.exists(path) || !Files.isRegularFile(path)) {
                        return java.util.Optional.empty();
                    }
                    return java.util.Optional.of(path);
                })
                .map(path -> {
                    MediaType mediaType = resolveMediaType(path);
                    Resource resource = new FileSystemResource(path);
                    return ResponseEntity.ok()
                            .contentType(mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                            .body(resource);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorldEntity> create(@RequestBody WorldEntity worldEntity) {
        WorldEntity saved = worldService.create(worldEntity);
        return ResponseEntity.created(URI.create("/world/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorldEntity> update(@PathVariable Long id, @RequestBody WorldEntity request) {
        return worldService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!worldService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/sensor-map", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<WorldEntity>> uploadSensorMap(@PathVariable Long id, @RequestPart("file") FilePart filePart) {
        Path worldImagePath = Paths.get(worldImageDir);
        String storedFileName = UUID.randomUUID() + getExtension(filePart.filename());
        Path targetPath = worldImagePath.resolve(storedFileName).normalize();
        String dbPath = targetPath.toString();

        return Mono.fromCallable(() -> {
                    Files.createDirectories(worldImagePath);
                return targetPath;
            })
            .flatMap(path -> filePart.transferTo(path).thenReturn(path))
            .flatMap(path -> Mono.fromCallable(() -> worldService.updateSensorMapPath(id, dbPath))
                    .flatMap(updated -> {
                        if (updated.isPresent()) {
                                return Mono.just(ResponseEntity.ok(updated.get()));
                            }
                            return Mono.fromCallable(() -> {
                                Files.deleteIfExists(path);
                                return ResponseEntity.notFound().build();
                            });
                    }));
    }

    private String getExtension(String originalFileName) {
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalFileName.length() - 1) {
            return ".bin";
        }
        String extension = originalFileName.substring(lastDot).toLowerCase(Locale.ROOT);
        return extension.length() > 10 ? ".bin" : extension;
    }

    private MediaType resolveMediaType(Path path) {
        try {
            String contentType = Files.probeContentType(path);
            if (contentType == null || contentType.isBlank()) {
                return MediaType.APPLICATION_OCTET_STREAM;
            }
            return MediaType.parseMediaType(contentType);
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
