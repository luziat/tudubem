package org.example.tudubem.robot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Robot", description = "로봇(Robot) 관리 API")
public class RobotController {

    private final RobotService robotService;

    @GetMapping
    @Operation(summary = "로봇 목록 조회", description = "등록된 모든 로봇을 조회합니다.")
    public List<RobotEntity> findAll() {
        return robotService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "로봇 단건 조회", description = "로봇 ID로 단건 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "로봇을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<RobotEntity> findById(@PathVariable Long id) {
        return robotService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "로봇 생성", description = "새 로봇을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    public ResponseEntity<RobotEntity> create(@RequestBody RobotEntity robotEntity) {
        RobotEntity saved = robotService.create(robotEntity);
        return ResponseEntity.created(URI.create("/robot/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "로봇 수정", description = "로봇 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "로봇을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<RobotEntity> update(@PathVariable Long id, @RequestBody RobotEntity request) {
        return robotService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "로봇 삭제", description = "로봇을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "로봇을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!robotService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
