package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Door extends HousePart {
	private static final long serialVersionUID = 1L;
//	private static final double GRID_SIZE = 0.2;
	private static double defaultDoorHeight = 0.8f;
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer normalBuffer;
	private transient FloatBuffer textureBuffer;

	public Door() {
		super(2, 4, defaultDoorHeight);
	}
	
	@Override
	public double getGridSize() {
		return 0.2;
	}	

	protected void init() {
		super.init();
		mesh = new Mesh("Door");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		normalBuffer = BufferUtils.createVector3Buffer(4);
		textureBuffer = BufferUtils.createVector2Buffer(4);
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

		mesh.setModelBound(new BoundingBox());	
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			PickedHousePart picked = pickContainer(x, y, Wall.class);
			if (picked != null) {
				Vector3 p = picked.getPoint();
				Vector3 wallFirstPoint = container.getAbsPoint(0);
				Vector3 wallx = container.getAbsPoint(2).subtract(wallFirstPoint, null);
				p = closestPoint(wallFirstPoint, wallx, x, y);
				p = grid(p, getGridSize(), false);

				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				points.set(index, toRelative(p));
				p.setZ(p.getZ() + height);
				points.set(index + 1, toRelative(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 absoluteBase = toAbsolute(base);
			Vector3 p = closestPoint(absoluteBase, Vector3.UNIT_Z, x, y);
			p = grid(p, getGridSize());
			height = Math.max(0, p.getZ() - absoluteBase.getZ());
			
			final double rel_z = toRelative(absoluteBase.addLocal(0, 0, height)).getZ();
			points.get(1).setZ(rel_z);
			points.get(3).setZ(rel_z);
		}
		if (container != null) {
			draw();
			setEditPointsVisible(true);
		}
	}

	protected void drawMesh() {
		if (points.size() < 4)
			return;		

		vertexBuffer.rewind();
		for (int i = 0; i < points.size(); i++) {
			final ReadOnlyVector3 p = getAbsPoint(i);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}

		// Compute normals
		Vector3 normal = getAbsPoint(2).subtract(getAbsPoint(0), null).crossLocal(getAbsPoint(1).subtract(getAbsPoint(0), null)).normalizeLocal();
		normal.negateLocal();
		normalBuffer.rewind();
		for (int i = 0; i < points.size(); i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());

		// Texture coords
		textureBuffer.rewind();
		textureBuffer.put(0).put(0);
		textureBuffer.put(0).put(1);
		textureBuffer.put(1).put(0);
		textureBuffer.put(1).put(1);

		mesh.updateModelBound();
	}

	public boolean isPrintable() {
		return false;
	}
	
	public void updateTextureAndColor(final boolean textureEnabled) {
		
	}

	protected String getDefaultTextureFileName() {
		return "door.jpg";
	}
}
