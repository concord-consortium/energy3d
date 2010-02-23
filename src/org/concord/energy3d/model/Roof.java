package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
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
	private double roofHeight = 0.5;
	private Mesh mesh = new Mesh("Roof");
	private Wall wall;
	 private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private Vector3 avg;

	// private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);

	public Roof() {
		super(1, 1);
		root.attachChild(mesh);
		 mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		 mesh.getMeshData().setVertexBuffer(vertexBuffer);

		// mesh.setRandomColors();
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

		points.add(new Vector3());
		// points.add(new Vector3());
	}

	@Override
	public void addPoint(int x, int y) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");

		if (points.size() >= numOfEditPoints)
			drawCompleted = true;
		else {
			// points.add(new Vector3());
			setPreviewPoint(x, y);
		}
	}

	@Override
	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			selectWall(x, y);
		} else {
			Vector3 base = avg;
			Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
			roofHeight = findHeight(base, closestPoint);
			// points.set(0, new Vector3(avg.getX(), avg.getY(), avg.getZ() + roofHeight));
		}
		draw();

	}

	public void selectWall(int x, int y) {
		pickResults.clear();
		for (HousePart housePart : House.getInstance().getParts())
			if (housePart instanceof Wall && housePart != this)
				pick(x, y, ((Wall) housePart).getRoot());

		if (pickResults.getNumber() > 0) {
			final PickData pick = pickResults.getPickData(0);
			final IntersectionRecord intersectionRecord = pick.getIntersectionRecord();
			if (intersectionRecord.getNumberOfIntersections() > 0) {
				UserData data = (UserData) pick.getTargetMesh().getUserData();
				if (data == null || !(data.getHousePart() instanceof Wall))
					throw new RuntimeException("Door can only be placed on a wall!");
				if (wall != null && data.getHousePart() != wall && points.size() > 2)
					throw new RuntimeException("Door points cannot be placed on multiple walls!");
				if (wall == null || wall != data.getHousePart()) {
					if (wall != null)
						wall.removeChild(this);
					wall = (Wall) data.getHousePart();
					wall.addChild(this);
				}
				return;
			}
		}
	}

	@Override
	protected void draw() {
		if (wall == null)
			return;
		// final double SNAP_DISTANCE = 0.1;
		ArrayList<PolygonPoint> wallUpperPoints = new ArrayList<PolygonPoint>();
		// wallUpperPoints.add(new PolygonPoint(0, 0));
		avg = new Vector3();
		// addPointToPolygon(wallUpperPoints, wall.getPoints().get(1));
		// addPointToPolygon(wallUpperPoints, wall.getPoints().get(3));
		// ArrayList<Wall> unexploredWalls = new ArrayList<Wall>();
		// ArrayList<Wall> exploredWalls = new ArrayList<Wall>();
		// unexploredWalls.add(this.wall);
		// while (!unexploredWalls.isEmpty()) {
		// Wall currentWall = unexploredWalls.get(0);
		// for (HousePart part : House.getInstance().getParts()) {
		// if (part instanceof Wall && part != currentWall && !exploredWalls.contains(part)) {
		// Wall wall = (Wall) part;
		// boolean found = false;
		// for (Vector3 pThis : currentWall.getPoints())
		// if (found)
		// break;
		// else
		// for (Vector3 pOther : wall.getPoints())
		// if (pThis.distance(pOther) < SNAP_DISTANCE) {
		// addPointToPolygon(wallUpperPoints, wall.getPoints().get(1));
		// addPointToPolygon(wallUpperPoints, wall.getPoints().get(3));
		// found = true;
		// unexploredWalls.add(wall);
		// break;
		// }
		// }
		// }
		// unexploredWalls.remove(currentWall);
		// exploredWalls.add(currentWall);
		// }

		exploreWallNeighbors(wallUpperPoints, wall);
		avg.multiplyLocal(1f / (wallUpperPoints.size()));
		points.get(0).set(avg.getX(), avg.getY(), avg.getZ() + roofHeight);
		PolygonPoint roofUpperPoint = new PolygonPoint(avg.getX(), avg.getY(), avg.getZ() + roofHeight);
		// wallUpperPoints.set(0, roofUpperPoint);

		System.out.println("Polygon Points:");
		for (PolygonPoint p : wallUpperPoints) {
			// if (p != roofUpperPoint) {
			// roofUpperPoint.addEdge(new DTSweepConstraint(roofUpperPoint, p));
			// }
			System.out.println(p.getXf() + "\t" + p.getYf() + "\t" + p.getZf());
		}

		// if (vertexBuffer.capacity() != wallUpperPoints.size() * 3)
		// vertexBuffer = BufferUtils.createVector3Buffer(wallUpperPoints.size() * 3);

		// vertexBuffer.position(0);
		// for (Vector3 p : wallUpperPoints)
		// vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

		Polygon ps = new Polygon(wallUpperPoints);
		ps.addSteinerPoint(roofUpperPoint);
		Poly2Tri.triangulate(ps);
		ArdorMeshMapper.updateTriangleMesh(mesh, ps);
		ArdorMeshMapper.updateVertexNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, ps.getTriangles(), 1, 0);
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

	private void exploreWallNeighbors(ArrayList<PolygonPoint> poly, Wall startWall) {
		Wall currentWall = startWall;
		Wall prevWall = null;
		while (currentWall != null) {
			Snap next = currentWall.next(prevWall);
			prevWall = currentWall;
			if (next == null)
				break;
			currentWall = (Wall) next.getHousePart();
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
			currentWall = (Wall) next.getHousePart();
			if (currentWall == startWall)
				break;
		}

		// poly.add(poly.get(1));
	}

	private void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p) {
		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint)) {
			avg.addLocal(p);
			poly.add(polygonPoint);
		}
	}

}
