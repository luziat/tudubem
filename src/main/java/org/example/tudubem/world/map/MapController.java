package org.example.tudubem.world.map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/map")
@RequiredArgsConstructor
@Tag(name = "Map", description = "지도(Map) 관리 API")
public class MapController {
    private final MapService mapService;
    @Value("${app.map.image-dir:./data/map}")
    private String mapImageDir;

    @GetMapping
    @Operation(summary = "지도 목록 조회", description = "등록된 모든 지도를 조회합니다.")
    public List<MapEntity> findAll() {
        return mapService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "지도 단건 조회", description = "지도 ID로 단건 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<MapEntity> findById(@PathVariable Long id) {
        return mapService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/sensor-map")
    @Operation(summary = "센서맵 이미지 조회", description = "지도에 연결된 센서맵 이미지를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지도 또는 센서맵 파일을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Resource> getSensorMap(@PathVariable Long id) {
        return mapService.findById(id)
                .flatMap(mapEntity -> {
                    String sensorMapImagePath = mapEntity.getSensorMapImagePath();
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
    @Operation(summary = "지도 생성", description = "새 지도를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    public ResponseEntity<MapEntity> create(@RequestBody MapEntity mapEntity) {
        MapEntity saved = mapService.create(mapEntity);
        return ResponseEntity.created(URI.create("/map/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "지도 수정", description = "지도 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<MapEntity> update(@PathVariable Long id, @RequestBody MapEntity request) {
        return mapService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "지도 삭제", description = "지도를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!mapService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/sensor-map", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "센서맵 이미지 업로드", description = "센서맵 이미지를 업로드하고 지도에 저장 경로를 연결합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드/연결 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public Mono<ResponseEntity<MapEntity>> uploadSensorMap(@PathVariable Long id, @RequestPart("file") FilePart filePart) {
        Path mapImagePath = Paths.get(mapImageDir);
        String storedFileName = UUID.randomUUID() + getExtension(filePart.filename());
        Path targetPath = mapImagePath.resolve(storedFileName).normalize();
        String dbPath = targetPath.toString();

        return Mono.fromCallable(() -> {
                    Files.createDirectories(mapImagePath);
                return targetPath;
            })
            .flatMap(path -> filePart.transferTo(path).thenReturn(path))
            .flatMap(path -> Mono.fromCallable(() -> mapService.updateSensorMapPath(id, dbPath))
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
