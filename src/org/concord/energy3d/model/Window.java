package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.shapes.Annotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Window extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.15;
	private transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer normalBuffer;
	private transient BMText label1; // , label2;

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
		
//		root.attachChild(mesh);
	}

	public void addPoint(int x, int y) {
		if (container != null)
			super.addPoint(x, y);
	}

	// public void setPreviewPoint(int x, int y) {
	// if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
	// PickedHousePart picked = pick(x, y, Wall.class);
	// if (picked != null) {
	// Vector3 p = picked.getPoint();
	//
	// if (points.size() == 2 || editPointIndex == 0) {
	// p = grid(p, GRID_SIZE);
	// } else {
	// Vector3 wallFirstPoint = container.getPoints().get(0);
	// Vector3 wallx = container.getPoints().get(2).subtract(wallFirstPoint, null);
	// p = closestPoint(abspoints.get(0), abspoints.get(0).add(wallx, null), x, y);
	// p.setZ(abspoints.get(0).getZ()); // + container.getPoints().get(0).getZ());
	// p = grid(p, GRID_SIZE);
	// }
	//
	// int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
	// Vector3 p_rel = toRelative(p);
	// points.get(index).set(p_rel);
	// p.setZ(p.getZ() + height);
	// Vector3 p_rel_up = toRelative(p);
	// points.get(index + 1).set(p_rel_up);
	//
	// if (editPointIndex == 0 && points.size() != 2) {
	// points.get(2).setZ(p_rel.getZ());
	// points.get(3).setZ(p_rel_up.getZ());
	// }
	//
	// }
	// } else if (editPointIndex == 1 || editPointIndex == 3) {
	// int lower = (editPointIndex == 1) ? 0 : 2;
	// Vector3 base = points.get(lower);
	// Vector3 absoluteBase = toAbsolute(base);
	// Vector3 p = closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y);
	// p = grid(p, GRID_SIZE);
	// height = findHeight(absoluteBase, p); // + absoluteBase.getZ();
	// double rel_z = toRelative(absoluteBase.addLocal(0, 0, height)).getZ();
	// points.get(1).setZ(rel_z);
	// points.get(3).setZ(rel_z);
	// }
	// if (container != null) {
	// draw();
	// showPoints();
	// container.draw();
	// }
	// }

	public void setPreviewPoint(int x, int y) {

		int index = editPointIndex;
		if (index == -1) {
			if (isFirstPointInserted())
				index = 3;
			else
				index = 0;
		}
		PickedHousePart pick = pick(x, y, Wall.class);
		Vector3 p = points.get(index);
		if (pick != null) {
			p = pick.getPoint();
			p = grid(p, GRID_SIZE);
			p = toRelative(p);
		}
		points.get(index).set(p);
		if (!isFirstPointInserted()) {
			points.get(1).set(p);
			// points.get(2).set(p);
		} else {
			if (index == 0 || index == 3) {
				points.get(1).set(points.get(0).getX(), 0, points.get(3).getZ());
				points.get(2).set(points.get(3).getX(), 0, points.get(0).getZ());
			} else {
				points.get(0).set(points.get(1).getX(), 0, points.get(2).getZ());
				points.get(3).set(points.get(2).getX(), 0, points.get(1).getZ());
			}
		}

		draw();
		showPoints();

		// if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
		// PickedHousePart picked = pick(x, y, Wall.class);
		// if (picked != null) {
		// Vector3 p = picked.getPoint();
		//
		// if (points.size() == 2 || editPointIndex == 0) {
		// p = grid(p, GRID_SIZE);
		// } else {
		// Vector3 wallFirstPoint = container.getPoints().get(0);
		// Vector3 wallx = container.getPoints().get(2).subtract(wallFirstPoint, null);
		// p = closestPoint(abspoints.get(0), abspoints.get(0).add(wallx, null), x, y);
		// p.setZ(abspoints.get(0).getZ()); // + container.getPoints().get(0).getZ());
		// p = grid(p, GRID_SIZE);
		// }
		//
		// int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
		// Vector3 p_rel = toRelative(p);
		// points.get(index).set(p_rel);
		// p.setZ(p.getZ() + height);
		// Vector3 p_rel_up = toRelative(p);
		// points.get(index + 1).set(p_rel_up);
		//
		// if (editPointIndex == 0 && points.size() != 2) {
		// points.get(2).setZ(p_rel.getZ());
		// points.get(3).setZ(p_rel_up.getZ());
		// }
		//
		// }
		// } else if (editPointIndex == 1 || editPointIndex == 3) {
		// int lower = (editPointIndex == 1) ? 0 : 2;
		// Vector3 base = points.get(lower);
		// Vector3 absoluteBase = toAbsolute(base);
		// Vector3 p = closestPoint(absoluteBase, absoluteBase.add(0, 0, 1, null), x, y);
		// p = grid(p, GRID_SIZE);
		// height = findHeight(absoluteBase, p); // + absoluteBase.getZ();
		// double rel_z = toRelative(absoluteBase.addLocal(0, 0, height)).getZ();
		// points.get(1).setZ(rel_z);
		// points.get(3).setZ(rel_z);
		// }

		if (container != null) {
//			enforceConstraints();
			draw();
			showPoints();
			container.draw();
		}
	}

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

	protected void drawAnnotations() {
		if (points.size() < 4)
			return;
		int annotCounter = 0;

		Vector3 v = new Vector3();
		Vector3 v1 = new Vector3();
		double len = v.set(container.getPoints().get(2)).subtractLocal(container.getPoints().get(0)).length();
		if (label1 == null) {
			label1 = Annotation.makeNewLabel();
			label1.setAlign(Align.NorthWest);
			root.attachChild(label1);
		}
		v.set(abspoints.get(1)).subtractLocal(container.getPoints().get(0));
		double xy = Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY());
		// if (v.getXf() < 0)
		// v.setX(len+v.getX());
		if (xy < 0)
			xy = len + xy;
		// label1.setText("(" + Math.round(10*v.getXf())/10 + ", " + Math.round(10*v.getZf())/10 + ")");
		label1.setText("(" + Math.round(10 * xy) / 10.0 + ", " + Math.round(10 * v.getZf()) / 10.0 + ")");

		// if (label2 == null) {
		// label2 = Annotation.makeNewLabel();
		// label2.setAlign(Align.SouthEast);
		// root.attachChild(label2);
		// }
		// v.set(abspoints.get(2)).subtractLocal(container.getPoints().get(0));
		// if (v.getXf() < 0)
		// v.setX(len+v.getX());
		// // label2.setText("(" + Math.round(10*v.getXf())/10 + ", " + Math.round(10*v.getZf())/10 + ")");
		// label2.setText("(" + v.getXf()+ ", " + v.getZf() + ")");

		// final ReadOnlyTransform worldTransform = container.getRoot().getWorldTransform();
		final ReadOnlyTransform trans = container.getRoot().getTransform();
		// System.out.println("World = " + worldTransform.getMatrix());
		// if (original != null)
		// for (Vector3 p : abspoints) {
		// trans.applyForward(p);
		// }

		Vector3 faceDirection = new Vector3(container.getFaceDirection());
		// System.out.println("face direction annot (1) = " + faceDirection);
		Vector3 moveToFront = new Vector3();
		trans.applyForwardVector(faceDirection);
		moveToFront.set(faceDirection).multiplyLocal(0.04);

		// label1.setTranslation(abspoints.get(1));
		label1.setTranslation(abspointsTrans(1, trans, v));
		// label2.setTranslation(abspoints.get(2));

		// Vector3 faceDirection = Vector3.fetchTempInstance().set(container.getFaceDirection());
		// container.getRoot().getWorldTransform().applyForward(faceDirection);

		// System.out.println("face direction annot (2) = " + faceDirection);
		SizeAnnotation annot = fetchSizeAnnot(annotCounter++);

		// if (abspoints.get(2).getX() - abspoints.get(0).getX() < 0)
		// faceDirection = faceDirection.negate(null);
		trans.applyForward(center);
		// annot.setRange(abspoints.get(0), abspoints.get(1), center, faceDirection, original == null, Align.Center, true);
		annot.setRange(abspointsTrans(0, trans, v), abspointsTrans(1, trans, v1), center, faceDirection, false, Align.Center, true);
		annot.setTranslation(moveToFront);

		annot = fetchSizeAnnot(annotCounter++);
		// annot.setRange(abspoints.get(0), abspoints.get(2), center, faceDirection, original == null, Align.Center, true);
		annot.setRange(abspointsTrans(0, trans, v), abspointsTrans(2, trans, v1), center, faceDirection, false, Align.Center, true);
		annot.setTranslation(moveToFront);

		// System.out.println(abspoints.get(0));

		// Vector3.releaseTempInstance(v);
		// Vector3.releaseTempInstance(v1);
		// Vector3.releaseTempInstance(faceDirection);
		// Vector3.releaseTempInstance(moveToFront);
	}

	private ReadOnlyVector3 abspointsTrans(int i, ReadOnlyTransform trans, Vector3 v) {
		v.set(abspoints.get(i));
		return trans.applyForward(v);
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

	public void setAnnotationsVisible(boolean visible) {
		super.setAnnotationsVisible(visible);
		if (label1 != null)
			label1.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}

//	public void enforceConstraints() {
//		for (Vector3 p : points) {
//			final double wallx = container.getPoints().get(2).subtract(container.getPoints().get(0), null).length();
//			final double margin = 0.2 / wallx;
//			double x = Math.max(p.getX(), margin);
//			x = Math.min(x, 1 - margin);
//			p.setX(x);
//			// return new Vector3(x, p.getY(), p.getZ());
//		}
//	}
	
	public Vector3 enforceContraints(Vector3 p) {
		final double wallx = container.getPoints().get(2).subtract(container.getPoints().get(0), null).length();
		final double margin = 0.2 / wallx;
		double x = Math.max(p.getX(), margin);
		x = Math.min(x, 1 - margin);
		return new Vector3(x, p.getY(), p.getZ());
	}
	
	protected void computeAbsPoints() {
		for (int i = 0; i < points.size(); i++) {
			Vector3 p = enforceContraints(points.get(i));
			p = toAbsolute(p);
			abspoints.get(i).set(p);
			pointsRoot.getChild(i).setTranslation(p);
		}
	}
	

}
