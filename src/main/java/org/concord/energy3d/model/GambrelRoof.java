package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class GambrelRoof extends CustomRoof {
	private static final long serialVersionUID = 1L;

	public GambrelRoof() {
		super(7);
		setHeight(20.0);
	}

	@Override
	protected void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints) {
		if (recalculateEditPoints) {
			recalculateEditPoints = false;
			points.get(0).set(toRelative(getCenter()));

			final Wall wall = (Wall) container;
			final Snap[] neighbors = wall.getNeighbors();
			if (neighbors[0] != null && neighbors[1] != null) {
				gableEditPointToWallMap = new HashMap<Integer, List<Wall>>();
				final Wall neighborWall1 = neighbors[0].getNeighborOf(wall);
				final Vector3[] computeEditPointsWall1 = computeEditPoints(neighborWall1);
				points.get(1).set(computeEditPointsWall1[0]);
				points.get(2).set(computeEditPointsWall1[1]);
				points.get(3).set(computeEditPointsWall1[2]);
				final ArrayList<Wall> walls1 = new ArrayList<Wall>(1);
				walls1.add(neighborWall1);
				gableEditPointToWallMap.put(1, walls1);
				gableEditPointToWallMap.put(2, walls1);
				gableEditPointToWallMap.put(3, walls1);

				final Wall neighborWall2 = neighbors[1].getNeighborOf(wall);
				final Vector3[] computeEditPointsWall2 = computeEditPoints(neighborWall2);
				points.get(4).set(computeEditPointsWall2[0]);
				points.get(5).set(computeEditPointsWall2[1]);
				points.get(6).set(computeEditPointsWall2[2]);
				final ArrayList<Wall> walls2 = new ArrayList<Wall>(1);
				walls2.add(neighborWall2);
				gableEditPointToWallMap.put(4, walls2);
				gableEditPointToWallMap.put(5, walls2);
				gableEditPointToWallMap.put(6, walls2);
			}
			applyHeight();
		} else
			applyHeight();
	}

	private Vector3[] computeEditPoints(final Wall wall) {
		final Vector3 p1 = wall.getAbsPoint(1);
		final Vector3 p2 = wall.getAbsPoint(3);

		ReadOnlyVector3 p1_overhang = null;
		ReadOnlyVector3 p2_overhang = null;
		double minDistance = Double.MAX_VALUE;

		for (int i = 0; i < wallUpperPoints.size(); i++) {
			final ReadOnlyVector3 pi = wallUpperPoints.get(i);
			final ReadOnlyVector3 pi2 = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
			final double distance1 = p1.distance(pi) + p2.distance(pi2);
			final double distance2 = p1.distance(pi2) + p2.distance(pi);
			if (distance1 < minDistance || distance2 < minDistance) {
				if (distance1 < distance2) {
					p1_overhang = pi;
					p2_overhang = pi2;
					minDistance = distance1;
				} else {
					p1_overhang = pi2;
					p2_overhang = pi;
					minDistance = distance2;
				}
			}
		}
		p1.set(p1_overhang);
		p2.set(p2_overhang);

		final Vector3 p1p2 = p2.subtractLocal(p1);
		final Vector3[] result = new Vector3[3];
		final Vector3 n = wall.getNormal().multiply(-0.1, null);
		result[0] = toRelative(p1p2.multiply(0.25, null).addLocal(p1).addLocal(n));
		result[1] = toRelative(p1p2.multiply(0.5, null).addLocal(p1).addLocal(n));
		result[2] = toRelative(p1p2.multiply(0.75, null).addLocal(p1).addLocal(n));
		return result;
	}

	@Override
	public void applyHeight() {
		final double z = container.getPoints().get(1).getZ();
		points.get(0).setZ(z + height);
		points.get(1).setZ(z + height * 0.75);
		points.get(2).setZ(z + height);
		points.get(3).setZ(z + height * 0.75);
		points.get(4).setZ(z + height * 0.75);
		points.get(5).setZ(z + height);
		points.get(6).setZ(z + height * 0.75);
	}

}
