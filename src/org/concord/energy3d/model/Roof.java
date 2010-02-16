package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.sweep.DTSweepConstraint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.sets.PointSet;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

public class Roof extends HousePart {
	private double roofHeight = 0.5;
	private Mesh mesh = new Mesh("Roof");
	private Wall wall;
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private Vector3 avg;

	// private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);

	public Roof() {
		super(1, 2);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		// mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// // Add a texture to the box.
		// final TextureState ts = new TextureState();
		// ts.setTexture(TextureManager.load("door.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		// mesh.setRenderState(ts);

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
			points.add(new Vector3());
			setPreviewPoint(x, y);
		}
	}

	@Override
	public void setPreviewPoint(int x, int y) {
		findMousePoint(x, y);
		draw();

	}

	public Vector3 findMousePoint(int x, int y) {
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
				return intersectionRecord.getIntersectionPoint(0);
			}
		}
		return null;
	}

	@Override
	protected void draw() {
		if (wall == null)
			return;
		final double SNAP_DISTANCE = 0.1;
		ArrayList<TriangulationPoint> wallUpperPoints = new ArrayList<TriangulationPoint>();
		wallUpperPoints.add(new TPoint(0, 0));		
		avg = new Vector3();
		wallUpperPoints.add(toPoint(wall.getPoints().get(1)));
		wallUpperPoints.add(toPoint(wall.getPoints().get(3)));
		ArrayList<Wall> unexploredWalls = new ArrayList<Wall>();
		ArrayList<Wall> exploredWalls = new ArrayList<Wall>();
		unexploredWalls.add(this.wall);
		while (!unexploredWalls.isEmpty()) {
			Wall currentWall = unexploredWalls.get(0);
			for (HousePart part : House.getInstance().getParts()) {
				if (part instanceof Wall && part != currentWall && !exploredWalls.contains(part)) {
					Wall wall = (Wall) part;
					boolean found = false;
					for (Vector3 pThis : currentWall.getPoints())
						if (found)
							break;
						else
							for (Vector3 pOther : wall.getPoints())
								if (pThis.distance(pOther) < SNAP_DISTANCE) {
									wallUpperPoints.add(toPoint(wall.getPoints().get(1)));
									wallUpperPoints.add(toPoint(wall.getPoints().get(3)));
									found = true;
									unexploredWalls.add(wall);
									break;
								}
				}
			}
			unexploredWalls.remove(currentWall);
			exploredWalls.add(currentWall);
		}
		
		avg.multiplyLocal(1f/(wallUpperPoints.size()-1));
		TPoint roofUpperPoint = new TPoint(avg.getX(), avg.getY(), avg.getZ()+roofHeight);
		wallUpperPoints.set(0, roofUpperPoint);
		
		for (TriangulationPoint p : wallUpperPoints) {
			if (p != roofUpperPoint) {
				roofUpperPoint.addEdge(new DTSweepConstraint(roofUpperPoint, p));
			}
		}

//		if (vertexBuffer.capacity() != wallUpperPoints.size() * 3)
//			vertexBuffer = BufferUtils.createVector3Buffer(wallUpperPoints.size() * 3);

		// vertexBuffer.position(0);
		// for (Vector3 p : wallUpperPoints)
		// vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

		PointSet ps = new PointSet(wallUpperPoints);
		Poly2Tri.triangulate(ps);
		ArdorMeshMapper.updateTriangleMesh(mesh, ps);

		// force bound update
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
	}

	private TPoint toPoint(Vector3 p) {
		avg.addLocal(p);
		return new TPoint(p.getX(), p.getY(), p.getZ());
	}

}
