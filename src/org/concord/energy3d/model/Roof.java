package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Roof extends HousePart {
	private static final long serialVersionUID = 1L;
	private double roofHeight = 0.5;
	private transient Mesh mesh; // = new Mesh("Roof");
	// private Wall wall;
	private transient FloatBuffer vertexBuffer; // = BufferUtils.createVector3Buffer(4);
	private transient Vector3 avg;

	// private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);

	public Roof() {
		super(1, 1);
		// points.add(new Vector3());
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Roof");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		// mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("roof2.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		mesh.setRenderState(ts);

		mesh.setUserData(new UserData(this));
	}

	// protected void allocateNewPoint() {
	// }

	// @Override
	// public void addPoint(int x, int y) {
	// if (drawCompleted)
	// return;
	// // throw new RuntimeException("Drawing of this object is already completed");
	//
	// if (points.size() >= numOfEditPoints)
	// drawCompleted = true;
	// else {
	// // points.add(new Vector3());
	// setPreviewPoint(x, y);
	// }
	// }

	@Override
	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			// selectWall(x, y);
			pick(x, y, Wall.class);
		} else {
			Vector3 base = avg;
			Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
			roofHeight = findHeight(base, closestPoint);
		}
		draw();
		showPoints();

	}

	// public void selectWall(int x, int y) {
	// pickResults.clear();
	// for (HousePart housePart : House.getInstance().getParts())
	// if (housePart instanceof Wall && housePart != this)
	// pick(x, y, ((Wall) housePart).getRoot());
	//
	// if (pickResults.getNumber() > 0) {
	// final PickData pick = pickResults.getPickData(0);
	// final IntersectionRecord intersectionRecord = pick.getIntersectionRecord();
	// if (intersectionRecord.getNumberOfIntersections() > 0) {
	// UserData data = (UserData) pick.getTargetMesh().getUserData();
	// if (data == null || !(data.getHousePart() instanceof Wall))
	// throw new RuntimeException("Door can only be placed on a wall!");
	// if (wall != null && data.getHousePart() != wall && points.size() > 2)
	// throw new RuntimeException("Door points cannot be placed on multiple walls!");
	// if (wall == null || wall != data.getHousePart()) {
	// if (wall != null)
	// wall.removeChild(this);
	// wall = (Wall) data.getHousePart();
	// wall.addChild(this);
	// }
	// return;
	// }
	// }
	// }

	@Override
	protected void draw() {
		if (root == null)
			init();

		if (container == null)
			return;
		// ArrayList<PolygonPoint> wallUpperPoints = new ArrayList<PolygonPoint>();
		avg = new Vector3();
		ArrayList<PolygonPoint> wallUpperPoints = exploreWallNeighbors((Wall) container);
		avg.multiplyLocal(1f / (wallUpperPoints.size()));
		shiftToOutterEdge(wallUpperPoints);
		points.get(0).set(avg.getX(), avg.getY(), avg.getZ() + roofHeight);
		PolygonPoint roofUpperPoint = new PolygonPoint(avg.getX(), avg.getY(), avg.getZ() + roofHeight);

		System.out.println("Polygon Points:");
		for (PolygonPoint p : wallUpperPoints) {
			System.out.println(p.getXf() + "\t" + p.getYf() + "\t" + p.getZf());
		}

		Polygon ps = new Polygon(wallUpperPoints);
		ps.addSteinerPoint(roofUpperPoint);
		Poly2Tri.triangulate(ps);

		System.out.println("Triangulated Points:");
		for (DelaunayTriangle t : ps.getTriangles()) {
			t.printDebug();
//			System.out.println(t..getXf() + "\t" + p.getYf() + "\t" + p.getZf());
		}
		
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
			((Sphere) pointsRoot.getChild(i)).updateModelBound();
			pointsRoot.setVisible(i, true);
		}

		// force bound update
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
	}

	private ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
		ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		Wall currentWall = startWall;
		Wall prevWall = null;
		while (currentWall != null) {
			Snap next = currentWall.next(prevWall);
			prevWall = currentWall;
			if (next == null)
				break;
			currentWall = (Wall) next.getNeighbor();
			if (currentWall == startWall)
				break;
		}

		startWall = currentWall;
		prevWall = null;
		while (currentWall != null) {
			Snap next = currentWall.next(prevWall);
			int pointIndex = 0;
			if (next != null)
				pointIndex = next.getThisPointIndex();
			pointIndex = pointIndex + 1;
			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex == 1 ? 3 : 1));
			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex));
			prevWall = currentWall;
			if (next == null)
				break;
			currentWall = (Wall) next.getNeighbor();
			if (currentWall == startWall)
				break;
		}

		// poly.add(poly.get(1));
		return poly;
	}

	private void shiftToOutterEdge(ArrayList<PolygonPoint> wallUpperPoints) {
		final double edgeLenght = 0.3;
		Vector3 op = new Vector3();
		for (PolygonPoint p : wallUpperPoints) {
			op.set(p.getX(), p.getY(), 0).subtractLocal(avg.getX(), avg.getY(), 0).normalizeLocal().multiplyLocal(edgeLenght);
			op.addLocal(p.getX(), p.getY(), p.getZ());
			p.set(op.getX(), op.getY(), op.getZ());
		}
	}

	private void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p) {
		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint)) {
			avg.addLocal(p);
			poly.add(polygonPoint);
		}
	}

}
