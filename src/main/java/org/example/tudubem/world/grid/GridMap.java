package org.example.tudubem.world.grid;

import java.util.List;

public record GridMap(
        // 가로 셀 개수
        int widthCells,
        // 세로 셀 개수
        int heightCells,
        // 1셀을 구성하는 픽셀 크기
        int cellSizePx,
        // 점유 그리드(0=비점유, 1=점유), 원점은 좌하단 기준
        List<List<Integer>> occupancy
) {
}
