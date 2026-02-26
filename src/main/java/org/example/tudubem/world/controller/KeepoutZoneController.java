package org.example.tudubem.world.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.entity.KeepoutZoneEntity;
import org.example.tudubem.world.service.KeepoutZoneService;
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
@Tag(name = "KeepoutZone", description = "Keepout Zone 관리 API")
public class KeepoutZoneController {

    private final KeepoutZoneService keepoutZoneService;

    @GetMapping
    @Operation(summary = "Keepout 목록 조회", description = "특정 지도(mapId)의 keepout zone 목록을 조회합니다.")
    public List<KeepoutZoneEntity> findAll(@PathVariable Long mapId) {
        return keepoutZoneService.findAllByMapId(mapId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Keepout 단건 조회", description = "특정 지도의 keepout zone을 ID로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "keepout zone을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<KeepoutZoneEntity> findById(@PathVariable Long mapId, @PathVariable Long id) {
        return keepoutZoneService.findById(mapId, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Keepout 생성", description = "특정 지도에 keepout zone을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<KeepoutZoneEntity> create(@PathVariable Long mapId, @RequestBody KeepoutZoneEntity request) {
        return keepoutZoneService.create(mapId, request)
                .map(saved -> ResponseEntity.created(URI.create("/map/" + mapId + "/keepout-zones/" + saved.getId())).body(saved))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Keepout 수정", description = "특정 지도의 keepout zone을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "keepout zone 또는 지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<KeepoutZoneEntity> update(@PathVariable Long mapId, @PathVariable Long id, @RequestBody KeepoutZoneEntity request) {
        return keepoutZoneService.update(mapId, id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Keepout 삭제", description = "특정 지도의 keepout zone을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "keepout zone 또는 지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long mapId, @PathVariable Long id) {
        if (!keepoutZoneService.delete(mapId, id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
