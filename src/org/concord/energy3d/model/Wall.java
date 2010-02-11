package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

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
	private double wallHeight = 0.8f;
	private ArrayList<HousePart> children = new ArrayList<HousePart>();
	private Mesh mesh = new Mesh("Wall");
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);

	public Wall() {
		super(2, 4);
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

		mesh.setUserData(new UserData(this));

		allocateNewPoint();
	}

	public void addChild(HousePart housePart) {
		children.add(housePart);
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
		return new Vector3(p.getX(), p.getY(), wallHeight);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			Vector3 p = SceneManager.getInstance().findMousePoint(x, y);
			if (p != null) {
				p = snap(p);
				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				points.set(index, p);
				points.set(index + 1, getUpperPoint(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			wallHeight = findHeight(base, snap(closestPoint(base, base.add(0, 0, 1, null), x, y)));
			points.set(1, getUpperPoint(points.get(1)));
			points.set(3, getUpperPoint(points.get(3)));

		}
		draw();
	}

	@Override
	protected void draw() {
		boolean drawable = points.size() >= 4;

		vertexBuffer.position(0);
		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			if (drawable)
				vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
			pointsRoot.setVisible(i, true);
		}

		if (drawable) {
			final float TEXTURE_SCALE_X = (float) points.get(2).subtract(points.get(0), null).length();
			final float TEXTURE_SCALE_Y = (float) points.get(3).subtract(points.get(2), null).length();
			// texture coords
			textureBuffer.position(0);
			textureBuffer.put(0).put(0);
			textureBuffer.put(0).put(TEXTURE_SCALE_Y);
			textureBuffer.put(TEXTURE_SCALE_X).put(0);
			textureBuffer.put(TEXTURE_SCALE_X).put(TEXTURE_SCALE_Y);

			// force bound update
			CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
			
			for (HousePart child : children)
				child.draw();
		}
		
	}

}