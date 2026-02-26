package org.example.tudubem.world.service.grid;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GridMap(
        // 가로 셀 개수
        @Schema(description = "가로 셀 개수", example = "256")
        int widthCells,
        // 세로 셀 개수
        @Schema(description = "세로 셀 개수", example = "128")
        int heightCells,
        // 1셀을 구성하는 픽셀 크기
        @Schema(description = "1셀을 구성하는 픽셀 크기", example = "4")
        int cellSizePx,
        // 점유 그리드(0=비점유, 1=점유), 원점은 좌하단 기준
        @Schema(description = "점유 그리드(0=비점유, 1=점유), 원점은 좌하단 기준")
        List<List<Integer>> occupancy
) {
}
