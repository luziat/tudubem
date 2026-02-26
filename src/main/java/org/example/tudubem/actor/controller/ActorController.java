package org.example.tudubem.actor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tudubem.actor.entity.ActorEntity;
import org.example.tudubem.actor.service.ActorService;
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
@RequestMapping("/actor")
@RequiredArgsConstructor
@Tag(name = "Actor", description = "동적 객체(Actor) 관리 API")
public class ActorController {

    private final ActorService actorService;

    @GetMapping
    @Operation(summary = "Actor 목록 조회", description = "등록된 모든 Actor를 조회합니다.")
    public List<ActorEntity> findAll() {
        return actorService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Actor 단건 조회", description = "Actor ID로 단건 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "Actor를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<ActorEntity> findById(@PathVariable Long id) {
        return actorService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Actor 생성", description = "새 Actor를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    public ResponseEntity<ActorEntity> create(@RequestBody ActorEntity actorEntity) {
        ActorEntity saved = actorService.create(actorEntity);
        return ResponseEntity.created(URI.create("/actor/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actor 수정", description = "Actor 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "Actor를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<ActorEntity> update(@PathVariable Long id, @RequestBody ActorEntity request) {
        return actorService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Actor 삭제", description = "Actor를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "Actor를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!actorService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
