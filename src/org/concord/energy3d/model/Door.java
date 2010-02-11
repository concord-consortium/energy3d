package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Door extends HousePart {
	private static double WALL_HEIGHT = 0.5f;
	private Wall wall;
	private Mesh mesh = new Mesh("Wall");
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);	
	

	public Door(int x, int y) {
		super(x, y, 3, 4);
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
		
		mesh.setUserData(new UserData(this));
		
		draw();
		
	}
	
	public void addPoint(int x, int y) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");
		Vector3 p = snap(findMousePoint(x, y));
		
		// convert from absolute coordinates to relative-to-wall coordinates
		p = convertToWallRelative(p);
		
		if (points.isEmpty()) {
			points.add(p);
			addUpperPoint(p);
		}
			
		draw();
		
		if (points.size() >= numofEditPoints)
			drawCompleted = true;
		else {
			points.add(p);
			addUpperPoint(p);			
		}
	}

	private Vector3 convertToWallRelative(Vector3 p) {
		System.out.println("p = " + p);
		ArrayList<Vector3> wallPoints = wall.getPoints();
		Vector3 origin = wallPoints.get(0);
		p = p.subtract(origin, null);
		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
//		Vector3 pointOnWall = new Vector3(wallx.dot(p.normalize(null))*p.length(), 0, wally.dot(p.normalize(null))*p.length());
		Vector3 pointOnWall = new Vector3(wallx.dot(p), 0, wally.dot(p));
		System.out.println("to Wall = " + pointOnWall);
		return pointOnWall;
	}

	private Vector3 convertFromWallRelativeToAbsolute(Vector3 p) {
		ArrayList<Vector3> wallPoints = wall.getPoints();
		Vector3 origin = wallPoints.get(0);
		p = p.subtract(origin, null);
		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
		Vector3 pointOnSpace = origin.add(wallx.multiply(p.getX(), null), null)
									.add(wally.multiply(p.getZ(), null), null);
//		System.out.println("to Absolute = " + pointOnSpace);
		return pointOnSpace;
	}
	
	private void addUpperPoint(Vector3 p) {
		points.add(new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
	}
	
	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), WALL_HEIGHT);
	}

	public void setPreviewPoint(int x, int y) {
//		if (drawCompleted)
//			throw new RuntimeException("Drawing of this object is already completed");
		Vector3 p = findMousePoint(x, y);
		if (editPointIndex == -1) {
			if (p == null)
				return;
			p = snap(p);
			
			// convert from absolute coordinates to relative-to-wall coordinates
			p = convertToWallRelative(p);

//			draw(p, points.size());
//			draw();
			points.set(points.size()-2, p);
			points.set(points.size()-1, new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
//			draw();
		} else {
////			draw(p, editPointIndex);
//			points.set(editPointIndex, p);
			if (editPointIndex == 0 || editPointIndex == 2) {
//				if (p != null) {
				if (p == null)
					return;
				
				p = snap(p);
				points.set(editPointIndex, p);
				points.set((editPointIndex == 0) ? 1 : 3, getUpperPoint(p));
//				}
//			} else if () {
//				points.set(editPointIndex, p);
//				points.set(3, getUpperPoint(p));
			} else if (editPointIndex == 1 || editPointIndex == 3) {
				int lower = (editPointIndex == 1) ? 0 : 2;
//				WALL_HEIGHT = points.get(0).subtract(p, null).length();
				Vector3 base = points.get(lower);
				WALL_HEIGHT = findHeight(base, snap(findUpperPoint(base, x, y)));
				p = points.get(1);
				points.set(1, new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
				p = points.get(3);
				points.set(3, new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
			} 
//			else if (editPointIndex == 3) {
////				WALL_HEIGHT = points.get(2).subtract(p, null).length();
//				WALL_HEIGHT = findAltitude(points.get(2), x, y);
//				p = points.get(3);
//				points.set(3, new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
//				p = points.get(1);
//				points.set(1, new Vector3(p.getX(), p.getY(), WALL_HEIGHT));				
//			}
		}
//			
		draw();
	}
	
	public Vector3 findMousePoint(int x, int y) {
		for (HousePart housePart: House.getInstance().getParts())
			if (housePart instanceof Wall && housePart != this)
				pick(x, y, ((Wall)housePart).getRoot());
		
		if (pickResults.getNumber() > 0) {
			final PickData pick = pickResults.getPickData(0);
			final IntersectionRecord intersectionRecord = pick.getIntersectionRecord();
			if (intersectionRecord.getNumberOfIntersections() > 0) {
				UserData data = (UserData)pick.getTargetMesh().getUserData();
				if (data == null || !(data.getHousePart() instanceof Wall))
					throw new RuntimeException("Door can only be placed on a wall!");
				if (wall != null && data.getHousePart() != wall)
					throw new RuntimeException("Door points cannot be placed on multiple walls!");
				if (wall == null) {
					wall = (Wall)data.getHousePart();
					wall.addChild(this);
				}
				return intersectionRecord.getIntersectionPoint(0);
			}
		}
		return null;
	}	
	
	private Vector3 snap(Vector3 p) {
//		Vector3 closest = null;
//		double closestDistance = Double.MAX_VALUE;
//		for (HousePart housePart: House.getInstance().getParts()) {
//			if (housePart instanceof Wall && housePart != this) {
//				Wall wall = (Wall)housePart;
//				for (Vector3 p2 : wall.getPoints()) {
//					double distance = p.distance(p2);
//					if (distance < closestDistance) {
//						closest = p2;
//						closestDistance = distance;
//					}						
//				}
//			}
//		}
//		if (closestDistance < 0.5)
//			return closest;
//		else
			return p;
	}
	

	@Override
	protected void draw() {
		if (vertexBuffer == null)
			return;
		vertexBuffer.position(0);
		for (int i=0; i<points.size(); i++) {
			Vector3 p = convertFromWallRelativeToAbsolute(points.get(i));
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		
		
		// update location of point spheres
		pointsRoot.getChild(i).setTranslation(p);
		}
		
		final float TEXTURE_SCALE_X = (float)points.get(2).subtract(points.get(0), null).length();
		final float TEXTURE_SCALE_Y = (float)points.get(3).subtract(points.get(2), null).length();

		// texture coords
		textureBuffer.position(0);
		textureBuffer.put(0).put(0);
		textureBuffer.put(0).put(1);
		textureBuffer.put(1).put(0);
		textureBuffer.put(1).put(1);
		
		
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);

//		pointsRoot.getChild(0).setTranslation(points.get(0));
	}

}
