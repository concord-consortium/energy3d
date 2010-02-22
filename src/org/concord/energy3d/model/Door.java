package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Door extends HousePart {
	private double doorHeight = 0.5f;
	private Wall wall;
	private Mesh mesh = new Mesh("Door");
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);

	public Door() {
		super(3, 4);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("door.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		mesh.setRenderState(ts);

		OffsetState offsetState = new OffsetState();
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(-1);
		mesh.setRenderState(offsetState);
		
		
		mesh.setUserData(new UserData(this));

		allocateNewPoint();
	}

	public void addPoint(int x, int y) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");

		if (points.size() >= numOfEditPoints)
			drawCompleted = true;
		else {
			allocateNewPoint();
			setPreviewPoint(x, y);
		}
	}

	private void allocateNewPoint() {
		Vector3 p = new Vector3();
		points.add(p);
		points.add(p);
	}

	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), doorHeight);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			Vector3 p = findMousePoint(x, y);
			if (p != null) {
				Vector3 wallFirstPoint = wall.getPoints().get(0);
				Vector3 wallx = wall.getPoints().get(2).subtract(wallFirstPoint, null);
				p = closestPoint(wallFirstPoint, wallFirstPoint.add(wallx, null), x, y);
				// p = snap(p);
				// convert from absolute coordinates to relative-to-wall coordinates
				p = convertToWallRelative(p);

				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				points.set(index, p);
				points.set(index + 1, getUpperPoint(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 absoluteBase = convertFromWallRelativeToAbsolute(base);
			// doorHeight = findHeight(absoluteBase, snap(closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y)));
			doorHeight = findHeight(absoluteBase, closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y));
			points.set(1, getUpperPoint(points.get(1)));
			points.set(3, getUpperPoint(points.get(3)));
		}
		if (wall != null)
			draw();
	}

	private Vector3 convertToWallRelative(Vector3 p) {
//		System.out.println("p = " + p);
		ArrayList<Vector3> wallPoints = wall.getPoints();
		Vector3 origin = wallPoints.get(0);
		p = p.subtract(origin, null);
		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
		// Vector3 pointOnWall = new Vector3(wallx.dot(p.normalize(null))*p.length(), 0, wally.dot(p.normalize(null))*p.length());
		Vector3 pointOnWall = new Vector3(wallx.dot(p), 0, wally.dot(p));
//		System.out.println("to Wall = " + pointOnWall);
		return pointOnWall;
	}

	private Vector3 convertFromWallRelativeToAbsolute(Vector3 p) {
		ArrayList<Vector3> wallPoints = wall.getPoints();
		Vector3 origin = wallPoints.get(0);
		// p = p.subtract(origin, null);
		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
		Vector3 pointOnSpace = origin.add(wallx.multiply(p.getX(), null), null).add(wally.multiply(p.getZ(), null), null);
		// System.out.println("to Absolute = " + pointOnSpace);
		return pointOnSpace;
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
		boolean drawable = points.size() >= 4;

		vertexBuffer.position(0);
		for (int i = 0; i < points.size(); i++) {
			Vector3 p = convertFromWallRelativeToAbsolute(points.get(i));
			if (drawable)
				vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
			pointsRoot.setVisible(i, true);
			pointsRoot.updateGeometricState(0, true);
		}

		if (drawable) {
			// texture coords
			textureBuffer.position(0);
			textureBuffer.put(0).put(0);
			textureBuffer.put(0).put(1);
			textureBuffer.put(1).put(0);
			textureBuffer.put(1).put(1);

			// Add a clip state to the scene.
//			clip();

			// force bound update
			CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		}

	}

	private void clip() {
		final ClipState cs = new ClipState();
		int i = 0;
		
		Vector3 p1 = convertFromWallRelativeToAbsolute(points.get(0));
		Vector3 p2 = convertFromWallRelativeToAbsolute(points.get(1));
		Vector3 p3 = convertFromWallRelativeToAbsolute(points.get(2));
		Vector3 p4 = convertFromWallRelativeToAbsolute(points.get(3));

		addClipPlate(cs, 1, p1, p2, p3, true);
//		addClipPlate(cs, 2, p3, p4, p1, true);
//		addClipPlate(cs, i, p1, p2, p3);
		wall.getRoot().setRenderState(cs);
		wall.getRoot().updateGeometricState(0);
	}

	private void addClipPlate(final ClipState cs, int i, Vector3 p1, Vector3 p2, Vector3 p3, boolean neg) {
		Vector3 p1p3 = p3.subtract(p1, null);
		Vector3 normal = p1p3.cross(p2.subtract(p1, null), null);
		normal.normalizeLocal();
		p3 = p1.add(normal, null);
//		normal = p1p2;
		p1p3.normalizeLocal();
		if (neg)
		p1p3.negateLocal();
		

		double D = p1.getX() * (p2.getY() * p3.getZ() - p3.getY() * p2.getZ()) + p2.getX() * (p3.getY() * p1.getZ() - p1.getY() * p3.getZ()) + p3.getX() * (p1.getY() * p2.getZ() - p2.getY() * p1.getZ());
		System.out.println(normal);
		System.out.println(D);
		cs.setEnableClipPlane(i, true);
		int fac = neg?-1:1;
		cs.setClipPlaneEquation(i, p1p3.getX(), p1p3.getY(), p1p3.getZ(), (neg?1:-1)*2*D);
//		cs.setClipPlaneEquation(0, -1, 0, 0, 1);
	}

}
