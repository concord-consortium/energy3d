package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import javax.print.attribute.standard.Fidelity;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	private static double WALL_HEIGHT = 0.5f;
	private Mesh mesh = new Mesh("Wall");
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);	

	public Wall(int x, int y) {
		super(x, y, 2, 4);
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
		ts.setTexture(TextureManager.load("brick_wall.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		mesh.setRenderState(ts);
		
		draw();
	}

//	@Override
//	protected void draw(Vector3 p, int i) {
//		vertexBuffer.position(i * 2 * 3);
//		vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
//		vertexBuffer.put(p.getXf()).put(p.getYf()).put(WALL_HEIGHT);
//		
//		final float TEXTURE_SCALE = (i < 1) ? 0 : (float)p.subtract(points.get(i-1), null).length();
//
//		// texture coords
//		textureBuffer.position(i * 2 * 2);
//		textureBuffer.put(TEXTURE_SCALE).put(0);
//		textureBuffer.put(TEXTURE_SCALE).put(1);
//		
//		// update location of point spheres
//		pointsRoot.getChild(i).setTranslation(p);
//		
//		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
//	}
	
//	public void addPoint(Vector3 p) {
//		super.addPoint(p);
////		vertexBuffer.put(p.getXf()).put(p.getYf()).put(WALL_HEIGHT);
////		points.add(new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
////		points.add(p);
//		addUpperPoint(p);
//		draw();
//	}

	public void addPoint(int x, int y) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");
		Vector3 p = SceneManager.getInstance().findMousePoint(x, y);
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
	
	private void addUpperPoint(Vector3 p) {
		points.add(new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
	}
	
	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), WALL_HEIGHT);
	}
	
	public void setPreviewPoint(int x, int y) {
//		if (drawCompleted)
//			throw new RuntimeException("Drawing of this object is already completed");
		Vector3 p = SceneManager.getInstance().findMousePoint(x, y);
		if (editPointIndex == -1) {
//			draw(p, points.size());
//			draw();
			points.set(points.size()-2, p);
			points.set(points.size()-1, new Vector3(p.getX(), p.getY(), WALL_HEIGHT));
//			draw();
		} else {
////			draw(p, editPointIndex);
//			points.set(editPointIndex, p);
			if (editPointIndex == 0 || editPointIndex == 2) {
				if (p != null) {
					p = snap(p);
				points.set(editPointIndex, p);
				points.set((editPointIndex == 0) ? 1 : 3, getUpperPoint(p));
				}
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
	
	private Vector3 snap(Vector3 p) {
		Vector3 closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (HousePart housePart: House.getInstance().getParts()) {
			if (housePart instanceof Wall && housePart != this) {
				Wall wall = (Wall)housePart;
				for (Vector3 p2 : wall.getPoints()) {
					double distance = p.distance(p2);
					if (distance < closestDistance) {
						closest = p2;
						closestDistance = distance;
					}						
				}
			}
		}
		if (closestDistance < 0.5)
			return closest;
		else
			return p;
	}
	
//	@Override
//	protected void draw() {
//		if (vertexBuffer == null)
//			return;
//		vertexBuffer.position(0);
//		for (int i=0; i<points.size(); i++) {
//			Vector3 p = points.get(i);
////		vertexBuffer.position(i * 2 * 3);
////		vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
////		vertexBuffer.put(p.getXf()).put(p.getYf()).put(WALL_HEIGHT);
//		
//		final float TEXTURE_SCALE = (i < 1) ? 0 : (float)p.subtract(points.get(i-1), null).length();
//
//		// texture coords
////		textureBuffer.position(i * 2 * 2);
////		textureBuffer.put(TEXTURE_SCALE).put(0);
////		textureBuffer.put(TEXTURE_SCALE).put(1);
//		
//		// update location of point spheres
//		pointsRoot.getChild(i).setTranslation(p);
//		}
//		
//		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
//
////		pointsRoot.getChild(0).setTranslation(points.get(0));
//	}

	@Override
	protected void draw() {
		if (vertexBuffer == null)
			return;
		vertexBuffer.position(0);
		for (int i=0; i<points.size(); i++) {
			Vector3 p = points.get(i);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		
		
		// update location of point spheres
		pointsRoot.getChild(i).setTranslation(p);
		}
		
		final float TEXTURE_SCALE_X = (float)points.get(2).subtract(points.get(0), null).length();
		final float TEXTURE_SCALE_Y = (float)points.get(3).subtract(points.get(2), null).length();

		// texture coords
		textureBuffer.position(0);
		textureBuffer.put(0).put(0);
		textureBuffer.put(0).put(TEXTURE_SCALE_Y);
		textureBuffer.put(TEXTURE_SCALE_X).put(0);
		textureBuffer.put(TEXTURE_SCALE_X).put(TEXTURE_SCALE_Y);
		
		
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);

//		pointsRoot.getChild(0).setTranslation(points.get(0));
	}
	
}