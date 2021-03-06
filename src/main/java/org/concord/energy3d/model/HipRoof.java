package org.concord.energy3d.model;

import java.util.List;

import org.concord.energy3d.util.Util;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3PolygonPoint;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class HipRoof extends Roof {

    private static final long serialVersionUID = 1L;
    transient boolean recalculateEditPoints;

    public HipRoof() {
        super(3);
    }

    @Override
    public void setPreviewPoint(final int x, final int y) {
        final Foundation foundation = getTopContainer();
        if (foundation != null && foundation.getLockEdit()) {
            return;
        }
        final EditState editState = new EditState();
        if (editPointIndex == -1) {
            pickContainer(x, y, Wall.class);
            recalculateEditPoints = true;
        } else if (editPointIndex == 0) {
            final ReadOnlyVector3 base = getCenter();
            final Vector3 p = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
            if (p != null) {
                snapToGrid(p, getAbsPoint(editPointIndex), getGridSize());
                height = Math.max(0, p.getZ() - base.getZ());
            }
        } else if (editPointIndex == 1 || editPointIndex == 2) {
            final Vector3 hipDirection = container.getAbsPoint(2).subtractLocal(container.getAbsPoint(0)).normalizeLocal();
            final Vector3 p = Util.closestPoint(getAbsPoint(0), hipDirection, x, y);
            if (p != null) {
                // snapToGrid(p, getAbsPoint(editPointIndex), getGridSize(), false);
                if (insideWallsPolygon(p)) {
                    points.get(editPointIndex).set(toRelative(p));
                }
            }
        }
        postEdit(editState);
    }

    @Override
    protected Polygon applySteinerPoint(final Polygon polygon) {
        final TriangulationPoint roofUpperPoint1 = new ArdorVector3PolygonPoint(getAbsPoint(1));
        final TriangulationPoint roofUpperPoint2 = new ArdorVector3PolygonPoint(getAbsPoint(2));
        polygon.addSteinerPoint(roofUpperPoint1);
        if (!roofUpperPoint2.equals(roofUpperPoint1)) {
            polygon.addSteinerPoint(roofUpperPoint2);
        }
        return polygon;
    }

    @Override
    protected void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints) {
        if (recalculateEditPoints) {
            recalculateEditPoints = false;
            final ReadOnlyVector3 center = getCenter();
            points.clear();
            points.add(toRelative(center));
            final Vector3 hipDirection = container.getAbsPoint(2).subtractLocal(container.getAbsPoint(0)).normalizeLocal();
            Vector3 point1 = findFarthestIntersection(wallUpperPoints, center, center.add(hipDirection.multiply(-1000, null), null));
            Vector3 point2 = findFarthestIntersection(wallUpperPoints, center, center.add(hipDirection.multiply(1000, null), null));
            if (point1 == null) {
                point1 = center.clone();
            }
            if (point2 == null) {
                point2 = center.clone();
            }

            point1.addLocal(hipDirection.multiply(point1.distance(center) * 0.5, null));
            point2.addLocal(hipDirection.multiply(-point2.distance(center) * 0.5, null));
            points.add(toRelative(point1));
            points.add(toRelative(point2));

            computeHeight(wallUpperPoints);
            applyHeight();
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

    private Vector3 findFarthestIntersection(final List<? extends ReadOnlyVector3> wallUpperPoints, final ReadOnlyVector3 center, final Vector3 p) {
        double farthestDistance = 0;
        Vector3 farthestIntersection = null;
        final int n = wallUpperPoints.size();
        for (int i = 0; i < n; i++) {
            final Vector3 intersect = Util.intersectLineSegments(center, p, wallUpperPoints.get(i), wallUpperPoints.get((i + 1) % n));
            if (intersect != null) {
                final double d = intersect.distanceSquared(center);
                if (d > farthestDistance) {
                    farthestDistance = d;
                    farthestIntersection = intersect;
                }
            }
        }
        return farthestIntersection;
    }

}