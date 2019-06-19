package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ardor3d.math.type.ReadOnlyVector3;

public class ShedRoof extends HipRoof {

    private static final long serialVersionUID = 1L;

    @Override
    protected void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints) {
        if (recalculateEditPoints) {
            super.processRoofEditPoints(wallUpperPoints);
            final Wall wall = (Wall) container;
            final Snap[] neighbors = wall.getNeighbors();
            if (neighbors[0] != null && neighbors[1] != null) {
                gableEditPointToWallMap = new HashMap<>();
                final ArrayList<Wall> walls1 = new ArrayList<>(2);
                walls1.add(wall);
                walls1.add(neighbors[0].getNeighborOf(wall));
                gableEditPointToWallMap.put(1, walls1);
                final ArrayList<Wall> walls2 = new ArrayList<>(2);
                walls2.add(wall);
                walls2.add(neighbors[1].getNeighborOf(wall));
                gableEditPointToWallMap.put(2, walls2);
            }
        } else {
            applyHeight();
        }
        // remove extra points that are sometimes added due to unknown reason
        final int numOfEditPoints = 3;
        while (points.size() > numOfEditPoints) {
            points.remove(numOfEditPoints);
            pointsRoot.detachChildAt(numOfEditPoints);
        }
    }

}