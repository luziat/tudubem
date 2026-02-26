package org.example.tudubem.actor.pathfind;

import org.example.tudubem.world.grid.GridMap;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Primary
public class BfsPathFindingStrategy implements PathFindingStrategy {

    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    @Override
    public PathResult findPath(GridMap gridMap, int startX, int startY, int targetX, int targetY) {
        int width = gridMap.widthCells();
        int height = gridMap.heightCells();

        if (!isInBounds(startX, startY, width, height) || !isInBounds(targetX, targetY, width, height)) {
            return new PathResult(false, List.of(), "start_or_target_out_of_bounds");
        }
        if (!isWalkable(gridMap, startX, startY)) {
            return new PathResult(false, List.of(), "start_blocked");
        }
        if (!isWalkable(gridMap, targetX, targetY)) {
            return new PathResult(false, List.of(), "target_blocked");
        }
        if (startX == targetX && startY == targetY) {
            return new PathResult(true, List.of(new GridPoint(startX, startY)), null);
        }

        boolean[][] visited = new boolean[height][width];
        int[][] prevX = new int[height][width];
        int[][] prevY = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                prevX[y][x] = -1;
                prevY[y][x] = -1;
            }
        }

        ArrayDeque<GridPoint> queue = new ArrayDeque<>();
        queue.add(new GridPoint(startX, startY));
        visited[startY][startX] = true;

        while (!queue.isEmpty()) {
            GridPoint current = queue.poll();
            if (current.x() == targetX && current.y() == targetY) {
                return new PathResult(true, rebuildPath(prevX, prevY, startX, startY, targetX, targetY), null);
            }

            for (int[] direction : DIRECTIONS) {
                int nx = current.x() + direction[0];
                int ny = current.y() + direction[1];
                if (!isInBounds(nx, ny, width, height)) {
                    continue;
                }
                if (visited[ny][nx] || !isWalkable(gridMap, nx, ny)) {
                    continue;
                }
                visited[ny][nx] = true;
                prevX[ny][nx] = current.x();
                prevY[ny][nx] = current.y();
                queue.add(new GridPoint(nx, ny));
            }
        }

        return new PathResult(false, List.of(), "path_not_found");
    }

    private List<GridPoint> rebuildPath(int[][] prevX, int[][] prevY, int startX, int startY, int targetX, int targetY) {
        List<GridPoint> path = new ArrayList<>();
        int cursorX = targetX;
        int cursorY = targetY;

        while (!(cursorX == startX && cursorY == startY)) {
            path.add(new GridPoint(cursorX, cursorY));
            int parentX = prevX[cursorY][cursorX];
            int parentY = prevY[cursorY][cursorX];
            if (parentX < 0 || parentY < 0) {
                return List.of();
            }
            cursorX = parentX;
            cursorY = parentY;
        }
        path.add(new GridPoint(startX, startY));
        Collections.reverse(path);
        return path;
    }

    private boolean isInBounds(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private boolean isWalkable(GridMap gridMap, int x, int y) {
        return gridMap.occupancy().get(y).get(x) == 0;
    }
}
