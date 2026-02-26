package org.example.tudubem.actor.pathfind;

import java.util.List;

public record PathResult(
        boolean found,
        List<GridPoint> path,
        String reason
) {
}
