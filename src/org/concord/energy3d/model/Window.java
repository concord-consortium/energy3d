package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.BoundingBox;
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
	private static final double GRID_SIZE = 0.15;
	private transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer normalBuffer;

	public Window() {
		super(2, 4, 0.30);
	}

	protected void init() {
		super.init();
		for (int i = 0; i < points.size(); i++)
			abspoints.get(i).set(toAbsolute(abspoints.get(i)));
		mesh = new Mesh("Window");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		normalBuffer = BufferUtils.createVector3Buffer(4);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setNormalBuffer(normalBuffer);
		mesh.setModelBound(new BoundingBox());

		// Transparency
		mesh.setDefaultColor(new ColorRGBA(0.3f, 0.4f, 0.5f, 0.7f));
		BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		blendState.setTestEnabled(true);
		mesh.setRenderState(blendState);
		mesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
		mesh.setRenderState(ms);

		mesh.setUserData(new UserData(this));
	}

	public void addPoint(int x, int y) {
		if (container != null)
			super.addPoint(x, y);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			PickedHousePart picked = pick(x, y, Wall.class);
			if (picked != null) {
				Vector3 p = picked.getPoint();

				if (points.size() == 2 || editPointIndex == 0) {
					p = grid(p, GRID_SIZE);
				} else {
					Vector3 wallFirstPoint = container.getPoints().get(0);
					Vector3 wallx = container.getPoints().get(2).subtract(wallFirstPoint, null);
					p = closestPoint(abspoints.get(0), abspoints.get(0).add(wallx, null), x, y);
					p.setZ(abspoints.get(0).getZ()); // + container.getPoints().get(0).getZ());
					p = grid(p, GRID_SIZE);
				}

				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				Vector3 p_rel = toRelative(p);
				points.get(index).set(p_rel);
				p.setZ(p.getZ() + height);
				Vector3 p_rel_up = toRelative(p);
				points.get(index + 1).set(p_rel_up);

				if (editPointIndex == 0 && points.size() != 2) {
					points.get(2).setZ(p_rel.getZ());
					points.get(3).setZ(p_rel_up.getZ());
				}

			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 absoluteBase = toAbsolute(base);
			Vector3 p = closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(absoluteBase, p); // + absoluteBase.getZ();
			double rel_z = toRelative(absoluteBase.addLocal(0, 0, height)).getZ();
			points.get(1).setZ(rel_z);
			points.get(3).setZ(rel_z);
		}
		if (container != null) {
			draw();
			showPoints();
			container.draw();
		}
	}

	// @Override
	// protected void updateMesh() {
	// if (root == null)
	// init();
	//
	// final boolean drawable = points.size() >= 4;
	// vertexBuffer.position(0);
	// Vector3[] convertedPoints = new Vector3[4];
	// for (int i = 0; i < points.size(); i++) {
	// Vector3 p = toAbsolute(points.get(i));
	// convertedPoints[i] = p;
	// abspoints.get(i).set(p);
	// if (drawable)
	// vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
	//
	// // update location of point spheres
	// // System.out.println(p);
	// pointsRoot.getChild(i).setTranslation(p);
	// pointsRoot.updateGeometricState(0);
	// }
	// // System.out.println("***" + (abspoints.get(1).getZ() - abspoints.get(0).getZ()));
	//
	// // compute normals
	// if (drawable) {
	// Vector3 normal = convertedPoints[2].subtract(convertedPoints[0], null).crossLocal(convertedPoints[1].subtract(convertedPoints[0], null)).normalizeLocal();
	// normal.negateLocal();
	// normalBuffer.position(0);
	// for (int i = 0; i < points.size(); i++)
	// normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
	// }
	//
	// if (drawable) {
	// // // texture coords
	// // textureBuffer.position(0);
	// // textureBuffer.put(0).put(0);
	// // textureBuffer.put(0).put(1);
	// // textureBuffer.put(1).put(0);
	// // textureBuffer.put(1).put(1);
	//
	// }
	// // force bound update
	// mesh.updateModelBound();
	// // root.updateGeometricState(0);
	// CollisionTreeManager.INSTANCE.removeCollisionTree(root);
	//
	// }

	protected void updateMesh() {
		if (points.size() < 4)
			return;
		vertexBuffer.rewind();
		for (Vector3 p : abspoints)
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

		// Compute normals
		final Vector3 normal = abspoints.get(2).subtract(abspoints.get(0), null).crossLocal(abspoints.get(1).subtract(abspoints.get(0), null)).normalizeLocal();
		normal.negateLocal();
		normalBuffer.rewind();
		for (int i = 0; i < points.size(); i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());

		mesh.updateModelBound();

	}

	public void delete() {
		if (container != null) {
			container.children.remove(this);
			container.draw();
		}
	}

	public ArrayList<Vector3> getPoints() {
		if (root == null)
			init();
		return abspoints;
	}

	public boolean isPrintable() {
		return false;
	}

}
