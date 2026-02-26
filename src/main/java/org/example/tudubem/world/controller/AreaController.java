package org.example.tudubem.world.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.world.entity.AreaEntity;
import org.example.tudubem.world.service.AreaService;
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
@Tag(name = "Area", description = "지도 영역(Area) 관리 API")
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    @Operation(summary = "영역 목록 조회", description = "특정 지도(mapId)의 영역 목록을 조회합니다.")
    public List<AreaEntity> findAll(@PathVariable Long mapId) {
        return areaService.findAllByMapId(mapId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "영역 단건 조회", description = "특정 지도의 영역을 ID로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "영역을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<AreaEntity> findById(@PathVariable Long mapId, @PathVariable Long id) {
        return areaService.findById(mapId, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "영역 생성", description = "특정 지도에 영역을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "404", description = "지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<AreaEntity> create(@PathVariable Long mapId, @RequestBody AreaEntity request) {
        return areaService.create(mapId, request)
                .map(saved -> ResponseEntity.created(URI.create("/map/" + mapId + "/areas/" + saved.getId())).body(saved))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "영역 수정", description = "특정 지도의 영역을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "영역 또는 지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<AreaEntity> update(@PathVariable Long mapId, @PathVariable Long id, @RequestBody AreaEntity request) {
        return areaService.update(mapId, id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "영역 삭제", description = "특정 지도의 영역을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "영역 또는 지도를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long mapId, @PathVariable Long id) {
        if (!areaService.delete(mapId, id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
