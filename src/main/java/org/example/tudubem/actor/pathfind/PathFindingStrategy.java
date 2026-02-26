package org.example.tudubem.actor.pathfind;

import org.example.tudubem.world.grid.GridMap;

public interface PathFindingStrategy {
    PathResult findPath(GridMap gridMap, int startX, int startY, int targetX, int targetY);
}
