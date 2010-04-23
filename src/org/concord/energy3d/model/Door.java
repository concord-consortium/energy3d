package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Door extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.25;
	private static double defaultDoorHeight = 0.8f;
//	private double height = defaultDoorHeight;
//	private Wall wall;
	private transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer normalBuffer;
	private transient FloatBuffer textureBuffer;
//	protected transient ArrayList<Vector3> abspoints;

	public Door() {
		super(2, 4);
		height = defaultDoorHeight;

//		allocateNewPoint();
	}
	protected void init() {
		super.init();
		mesh = new Mesh("Door");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		normalBuffer = BufferUtils.createVector3Buffer(4);
		textureBuffer = BufferUtils.createVector2Buffer(4);
//		abspoints = new ArrayList<Vector3>(4);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setNormalBuffer(normalBuffer);
		mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("door.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		mesh.setRenderState(ts);

		OffsetState offsetState = new OffsetState();
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(-1);
		offsetState.setUnits(-1);
		mesh.setRenderState(offsetState);
		
		
		mesh.setUserData(new UserData(this));		
	}
	
//	public void addPoint(int x, int y) {
//		if (drawCompleted)
//			return;
////			throw new RuntimeException("Drawing of this object is already completed");
//
//		if (points.size() >= numOfEditPoints)
//			drawCompleted = true;
//		else {
//			allocateNewPoint();
//			setPreviewPoint(x, y);
//		}
//	}

//	private void allocateNewPoint() {
//		Vector3 p = new Vector3();
//		points.add(p);
//		points.add(p);
//	}

	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), height);
	}

	@Override
	public void addPoint(int x, int y) {
		if (container != null)
			super.addPoint(x, y);
	}
	
	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
//			Vector3 p = findMousePoint(x, y);
			PickedHousePart picked = pick(x, y, Wall.class);			
			if (picked != null) {
				Vector3 p = picked.getPoint();
				Vector3 wallFirstPoint = container.getPoints().get(0);
				Vector3 wallx = container.getPoints().get(2).subtract(wallFirstPoint, null);
				p = closestPoint(wallFirstPoint, wallFirstPoint.add(wallx, null), x, y);
				p = grid(p, GRID_SIZE);
				// p = snap(p);
				// convert from absolute coordinates to relative-to-wall coordinates
				p = toRelative(p);

				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				points.set(index, p);
				points.set(index + 1, getUpperPoint(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 absoluteBase = toAbsolute(base);
			// doorHeight = findHeight(absoluteBase, snap(closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y)));
			Vector3 p = closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(absoluteBase, p);
			defaultDoorHeight = height;
			points.set(1, getUpperPoint(points.get(1)));
			points.set(3, getUpperPoint(points.get(3)));
		}
		if (container != null) {
			draw();
			showPoints();
		}
	}

//	private Vector3 convertToWallRelative(Vector3 p) {
////		System.out.println("p = " + p);
//		ArrayList<Vector3> wallPoints = container.getPoints();
//		Vector3 origin = wallPoints.get(0);
//		p = p.subtract(origin, null);
//		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
//		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
//		// Vector3 pointOnWall = new Vector3(wallx.dot(p.normalize(null))*p.length(), 0, wally.dot(p.normalize(null))*p.length());
//		Vector3 pointOnWall = new Vector3(wallx.dot(p), 0, wally.dot(p));
////		System.out.println("to Wall = " + pointOnWall);
//		return pointOnWall;
//	}
//
//	private Vector3 convertFromWallRelativeToAbsolute(Vector3 p) {
//		ArrayList<Vector3> wallPoints = container.getPoints();
//		Vector3 origin = wallPoints.get(0);
//		// p = p.subtract(origin, null);
//		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
//		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
//		Vector3 pointOnSpace = origin.add(wallx.multiply(p.getX(), null), null).add(wally.multiply(p.getZ(), null), null);
//		// System.out.println("to Absolute = " + pointOnSpace);
//		return pointOnSpace;
//	}

//	public Vector3 findMousePoint(int x, int y) {
//		pickResults.clear();
//		for (HousePart housePart : House.getInstance().getParts())
//			if (housePart instanceof Wall && housePart != this)
//				pick(x, y, ((Wall) housePart).getRoot());
//
//		if (pickResults.getNumber() > 0) {
//			final PickData pick = pickResults.getPickData(0);
//			final IntersectionRecord intersectionRecord = pick.getIntersectionRecord();
//			if (intersectionRecord.getNumberOfIntersections() > 0) {
//				UserData data = (UserData) pick.getTargetMesh().getUserData();
//				if (data == null || !(data.getHousePart() instanceof Wall))
//					throw new RuntimeException("Door can only be placed on a wall!");
//				if (wall != null && data.getHousePart() != wall && points.size() > 2)
//					throw new RuntimeException("Door points cannot be placed on multiple walls!");
//				if (wall == null || wall != data.getHousePart()) {
//					if (wall != null)
//						wall.removeChild(this);
//					wall = (Wall) data.getHousePart();
//					wall.addChild(this);
//				}
//				return intersectionRecord.getIntersectionPoint(0);
//			}
//		}
//		return null;
//	}

	@Override
	protected void draw() {
		if (root == null)
			init();
		boolean drawable = points.size() >= 4;

		vertexBuffer.position(0);
		Vector3[] convertedPoints = new Vector3[4];
		for (int i = 0; i < points.size(); i++) {
			Vector3 p = toAbsolute(points.get(i));
			convertedPoints[i] = p;
//			if (i < abspoints.size())
//				abspoints.set(i, p);
//			else
//				abspoints.add(p);
			abspoints.get(i).set(p);
			if (drawable)
				vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
			pointsRoot.updateGeometricState(0, true);
		}
		
		// compute normals
		if (drawable) {
			Vector3 normal = convertedPoints[2].subtract(convertedPoints[0], null).crossLocal(convertedPoints[1].subtract(convertedPoints[0], null)).normalizeLocal();
			normal.negateLocal();
			normalBuffer.position(0);
			for (int i = 0; i < points.size(); i++)
				normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		}
		

		if (drawable) {
			// texture coords
			textureBuffer.rewind();
			textureBuffer.put(0).put(0);
			textureBuffer.put(0).put(1);
			textureBuffer.put(1).put(0);
			textureBuffer.put(1).put(1);

			// force bound update
			mesh.updateModelBound();
			CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		}

	}

	@Override
	public ArrayList<Vector3> getPoints() {
		return abspoints;
	}
	
	

}
