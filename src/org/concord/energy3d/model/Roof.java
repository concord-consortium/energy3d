package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Roof extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
//	private double height = 0.5;
	private transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
	private transient Vector3 avg;

	public Roof() {
		super(1, 1, 0.5);
//		height = 0.5;
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Roof");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("roof2.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		mesh.setRenderState(ts);

		mesh.setUserData(new UserData(this));
	}

	@Override
	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			pick(x, y, Wall.class);
		} else {
			Vector3 base = avg;
			Vector3 p = closestPoint(base, base.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(base, p);
		}
		draw();
		showPoints();

	}

	@Override
	public void draw() {
		if (root == null)
			init();

		if (container == null)
			return;
		
		super.draw();
		
		ArrayList<PolygonPoint> wallUpperPoints = exploreWallNeighbors((Wall) container);
		
		avg = new Vector3();
		for (PolygonPoint p : wallUpperPoints)
			avg.addLocal(p.getX(), p.getY(), p.getZ());
		avg.multiplyLocal(1f / (wallUpperPoints.size()));
		shiftToOutterEdge(wallUpperPoints);
		points.get(0).set(avg.getX(), avg.getY(), avg.getZ() + height);
		PolygonPoint roofUpperPoint = new PolygonPoint(avg.getX(), avg.getY(), avg.getZ() + height);

//		System.out.println("Polygon Points:");
//		for (PolygonPoint p : wallUpperPoints) {
//			System.out.println(p.getXf() + "\t" + p.getYf() + "\t" + p.getZf());
//		}

		Polygon ps = new Polygon(wallUpperPoints);
		ps.addSteinerPoint(roofUpperPoint);
		Poly2Tri.triangulate(ps);

//		System.out.println("Triangulated Points:");
//		for (DelaunayTriangle t : ps.getTriangles()) {
//			t.printDebug();
//		}
		
		ArdorMeshMapper.updateTriangleMesh(mesh, ps);
		ArdorMeshMapper.updateVertexNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, ps.getTriangles(), 1, 0);
		
		mesh.getMeshData().updateVertexCount();
		// mesh.setRandomColors();

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
//			((Sphere) pointsRoot.getChild(i)).updateModelBound();
//			pointsRoot.setVisible(i, true);
		}

		// force bound update
		mesh.updateModelBound();
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
	}

//	private ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
//		ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
//		Wall currentWall = startWall;
//		Wall prevWall = null;
//		while (currentWall != null) {
//			Snap next = currentWall.next(prevWall);
//			prevWall = currentWall;
//			if (next == null)
//				break;
//			currentWall = (Wall) next.getNeighbor();
//			if (currentWall == startWall)
//				break;
//		}
//
//		startWall = currentWall;
//		prevWall = null;
//		while (currentWall != null && currentWall.isFirstPointInserted()) {
//			Snap next = currentWall.next(prevWall);
//			int pointIndex = 0;
//			if (next != null)
//				pointIndex = next.getThisPointIndex();
//			pointIndex = pointIndex + 1;
//			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex == 1 ? 3 : 1));
//			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex));
//			prevWall = currentWall;
//			if (next == null)
//				break;
//			currentWall = (Wall) next.getNeighbor();
//			if (currentWall == startWall)
//				break;
//		}
//
//		return poly;
//	}
//	
//	private void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p) {
//		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
//		if (!poly.contains(polygonPoint)) {
//			avg.addLocal(p);
//			poly.add(polygonPoint);
//		}
//	}

	private void shiftToOutterEdge(ArrayList<PolygonPoint> wallUpperPoints) {
		final double edgeLenght = 0.3;
		Vector3 op = new Vector3();
		for (PolygonPoint p : wallUpperPoints) {
			op.set(p.getX(), p.getY(), 0).subtractLocal(avg.getX(), avg.getY(), 0).normalizeLocal().multiplyLocal(edgeLenght);
			op.addLocal(p.getX(), p.getY(), p.getZ());
			p.set(op.getX(), op.getY(), op.getZ()+0.01);
		}
	}

	protected void flatten() {		
		root.setRotation((new Matrix3().fromAngles(Math.PI / 2, 0, 0)));
		root.setTranslation(pos, 0, 0);
	}		
}
