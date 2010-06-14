package org.concord.energy3d.model;

import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.Vector3;

public class Floor extends Roof {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;

	public Floor() {
		super(1, 1, 0.5);
	}

	public void setPreviewPoint(int x, int y) {
			pick(x, y, Wall.class);
			if (container != null) {
			Vector3 base = container.getPoints().get(0);
			Vector3 p = closestPoint(base, base.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(base, p) + base.getZ();
		draw();
		showPoints();
			}

	}

//	public void draw() {
//		if (root == null)
//			init();
//
//		if (container == null)
//			return;
//		
////		super.init();
//
//		
//		ArrayList<PolygonPoint> wallUpperPoints = exploreWallNeighbors((Wall) container);
//		shiftToOutterEdge(wallUpperPoints);
//		Polygon ps = new Polygon(wallUpperPoints);
//		Poly2Tri.triangulate(ps);
//
//		ArdorMeshMapper.updateTriangleMesh(mesh, ps);
//		ArdorMeshMapper.updateVertexNormals(mesh, ps.getTriangles());
//		ArdorMeshMapper.updateFaceNormals(mesh, ps.getTriangles());
//		ArdorMeshMapper.updateTextureCoordinates(mesh, ps.getTriangles(), 1, 0);
//		
//		mesh.getMeshData().updateVertexCount();
//
//		for (int i = 0; i < points.size(); i++) {
//			Vector3 p = points.get(i);
//			pointsRoot.getChild(i).setTranslation(p);
//		}
//
//		updateLabelLocation();
//		if (flattenTime > 0)
//			flatten();
//		
//		// force bound update
//		mesh.updateModelBound();
//		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
//	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		center.set(0, 0, 0);
		double maxY = wallUpperPoints.get(0).getY();
		for (PolygonPoint p : wallUpperPoints) {
			center.addLocal(p.getX(), p.getY(), height);
			p.set(p.getX(), p.getY(), height);
			if (p.getY() > maxY)
				maxY = p.getY();			
		}
		center.multiplyLocal(1.0 / wallUpperPoints.size());
		labelTop = (maxY-center.getY());
		points.get(0).set(center);
		final Polygon polygon = new Polygon(wallUpperPoints);
		return polygon;
	}
	
	public void setHeight(double newHeight, boolean finalize) {
		super.setHeight(newHeight, finalize);
//		points.get(0).setZ(newHeight);
		draw();		
	}	
//
//	protected void flatten() {		
//		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
////		root.setTranslation(flattenTime * 5*(int) (pos / 3), height, flattenTime * 3*(2 + pos % 3));
////		root.setTranslation(flattenTime * printX, height, flattenTime * printY);
//		super.flatten();
//	}		
//
//	protected double computeLabelTop() {
//		return labelTop;
//	}	
	
}
