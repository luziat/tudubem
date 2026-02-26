package org.example.tudubem.actor.service.pathfind;

import org.example.tudubem.world.service.grid.GridMap;

public interface PathFindingStrategy {
    PathResult findPath(GridMap gridMap, int startX, int startY, int targetX, int targetY);
}
