package org.example.tudubem.world.dto;

import lombok.AllArgsConstructor;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class WorldBundle {
    public final Long mapId;
    public final int widthCells;
    public final int heightCells;
    public final int cellSizePx;

    public final List<List<Integer>> base;
    public final List<List<Integer>> keepout;
    public List<List<Integer>> dynamic;
    public List<List<Integer>> composite;

    public final Map<String, List<Point2D.Double>> dynamicObjectsInGrid;

    public boolean hasSameGridConfig(Long mapId, int widthCells, int heightCells, int cellSizePx) {
        return this.mapId.equals(mapId)
                && this.widthCells == widthCells
                && this.heightCells == heightCells
                && this.cellSizePx == cellSizePx;
    }
}
