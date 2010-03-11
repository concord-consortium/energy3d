package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

public class Window extends HousePart {
	private static final long serialVersionUID = 1L;
	private double height = 0.5f;
	private transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer normalBuffer;

	public Window() {
		super(2, 4);
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Window");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		normalBuffer = BufferUtils.createVector3Buffer(4);		
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setNormalBuffer(normalBuffer);
		// mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Transparency
		mesh.setDefaultColor(new ColorRGBA(0.3f, 0.4f, 0.5f, 0.7f));
		BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		blendState.setTestEnabled(true);
		mesh.setRenderState(blendState);
		mesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		// ms.setMaterialFace(MaterialFace.FrontAndBack);
		ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
		mesh.setRenderState(ms);

		// // Add a texture to the box.
		// final TextureState ts = new TextureState();
		// ts.setTexture(TextureManager.load("window1.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		// mesh.setRenderState(ts);

		// OffsetState offsetState = new OffsetState();
		// offsetState.setTypeEnabled(OffsetType.Fill, true);
		// offsetState.setFactor(-1);
		// mesh.setRenderState(offsetState);

		mesh.setUserData(new UserData(this));
	}

	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), height);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			// Vector3 p = findMousePoint(x, y);
			PickedHousePart picked = pick(x, y, Wall.class);
			if (picked != null) {
				Vector3 p = picked.getPoint();
				if (points.size() <= 2) {
					height = points.get(0).getZ() + 0.25 + container.getPoints().get(0).getZ();
				} else {
					Vector3 wallFirstPoint = container.getPoints().get(0);
					Vector3 wallx = container.getPoints().get(2).subtract(wallFirstPoint, null);
					p = closestPoint(wallFirstPoint, wallFirstPoint.add(wallx, null), x, y);
					p.setZ(points.get(0).getZ() + container.getPoints().get(0).getZ());
				}
				p = convertToWallRelative(p);

				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				points.set(index, p);
				points.set(index + 1, getUpperPoint(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 absoluteBase = convertFromWallRelativeToAbsolute(base);
			height = findHeight(absoluteBase, closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y)) + absoluteBase.getZ();
			points.set(1, getUpperPoint(points.get(1)));
			points.set(3, getUpperPoint(points.get(3)));
		}
		if (container != null) {
			draw();
			showPoints();
			container.draw();
		}
	}

	@Override
	protected void draw() {
		if (root == null)
			init();

		final boolean drawable = points.size() >= 4;
		vertexBuffer.position(0);
		Vector3[] convertedPoints = new Vector3[4];
		for (int i = 0; i < points.size(); i++) {
			Vector3 p = convertFromWallRelativeToAbsolute(points.get(i));
			convertedPoints[i] = p;
			if (drawable)
				vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
			pointsRoot.updateGeometricState(0);
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
			// // texture coords
			// textureBuffer.position(0);
			// textureBuffer.put(0).put(0);
			// textureBuffer.put(0).put(1);
			// textureBuffer.put(1).put(0);
			// textureBuffer.put(1).put(1);

		}
		// force bound update
		root.updateGeometricState(0);
		CollisionTreeManager.INSTANCE.removeCollisionTree(root);

	}

}
