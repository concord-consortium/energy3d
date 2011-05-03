package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Foundation extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
	transient private boolean resizeHouseMode = false;
	transient private Box boundingMesh;
	transient private double newBoundingHeight;
	transient private ArrayList<Vector3> orgPoints;
	transient private Mesh wireframeMesh;
	private double boundingHeight;

	public Foundation() {
		super(2, 8, 0.1);
	}

	@Override
	protected void init() {
		super.init();
		resizeHouseMode = false;
		if (boundingHeight == 0)
			boundingHeight = 2;
		mesh = new Box("Foundation", new Vector3(), new Vector3());
		boundingMesh = new Box("Foundation (Bounding)", new Vector3(), new Vector3());
		root.attachChild(mesh);

		updateTextureAndColor(Scene.getInstance().isTextureEnabled());

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		WireframeState wire = new WireframeState();
		boundingMesh.setRenderState(wire);
		
		wireframeMesh = new Mesh("Foundation (wireframe)");
		wireframeMesh.getMeshData().setIndexMode(IndexMode.Quads);
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(16));
		wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		wireframeMesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wireframeMesh.getSceneHints().setCastsShadows(false);
		wireframeMesh.setRenderState(new WireframeState());
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);		
		root.attachChild(wireframeMesh);

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		boundingMesh.setUserData(userData);

		scanChildrenHeight(); // to fix bug with resizing height instead of width when moving edit point of platform right after loading the model
	}

	public void setResizeHouseMode(boolean resizeHouseMode) {
		this.resizeHouseMode = resizeHouseMode;
		if (resizeHouseMode) {
			scanChildrenHeight();
			root.attachChild(boundingMesh);
			showPoints();
		} else {
			root.detachChild(boundingMesh);
			hidePoints();
		}
	}

	public boolean isResizeHouseMode() {
		return resizeHouseMode;
	}

	@Override
	public void showPoints() {
		for (int i = 0; i < points.size(); i++) {
			if (!resizeHouseMode && i >= 4)
				pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
			else {
				computeEditPointScale(i);
				pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Inherit);
			}
		}
	}

	@Override
	public void complete() {
		super.complete();
		applyNewHeight(boundingHeight, newBoundingHeight, true);
		if (!resizeHouseMode) {
			final double dx = Math.abs(points.get(2).getX() - points.get(0).getX());
			final double dxOrg = Math.abs(orgPoints.get(2).getX() - orgPoints.get(0).getX());
			final double ratioX = dx / dxOrg;
			final double dy = Math.abs(points.get(1).getY() - points.get(0).getY());
			final double dyOrg = Math.abs(orgPoints.get(1).getY() - orgPoints.get(0).getY());
			final double ratioY = dy / dyOrg;
			for (final HousePart child : children)
				for (final Vector3 childPoint : child.getPoints()) {
					double x = childPoint.getX() / ratioX;
					if (editPointIndex == 0 || editPointIndex == 1)
						x += (dx - dxOrg) / dx;
					childPoint.setX(x);
					double y = childPoint.getY() / ratioY;
					if (editPointIndex == 0 || editPointIndex == 2)
						y += (dy - dyOrg) / dy;
					childPoint.setY(y);
				}
			orgPoints = null;
		}
	}

	@Override
	public void setPreviewPoint(int x, int y) {
		int index = editPointIndex;
		if (index == -1) {
			if (isFirstPointInserted())
				index = 3;
			else
				index = 0;
		}
		PickedHousePart pick = SelectUtil.pickPart(x, y, (Spatial) null);
		Vector3 p = points.get(index);
		if (pick != null) {
			p = pick.getPoint();
			p = grid(p, GRID_SIZE);
		}
		points.get(index).set(p);
		if (!isFirstPointInserted()) {
			points.get(1).set(p);
			points.get(2).set(p);
			points.get(3).set(p);
		} else {
			if (index == 0 || index == 3) {
				points.get(1).set(points.get(0).getX(), points.get(3).getY(), 0);
				points.get(2).set(points.get(3).getX(), points.get(0).getY(), 0);
			} else if (index == 1 || index == 2) {
				points.get(0).set(points.get(1).getX(), points.get(2).getY(), 0);
				points.get(3).set(points.get(2).getX(), points.get(1).getY(), 0);
			} else {
				final int lower = (editPointIndex == 1) ? 0 : 2;
				final Vector3 base = abspoints.get(lower);
				Vector3 closestPoint = closestPoint(base, Vector3.UNIT_Z, x, y);
				closestPoint = grid(closestPoint, GRID_SIZE);
				newBoundingHeight = Math.max(0, closestPoint.getZ() - base.getZ());
				applyNewHeight(boundingHeight, newBoundingHeight, false);
			}
			for (int i = 0; i < 4; i++)
				points.get(i + 4).set(points.get(i)).setZ(newBoundingHeight);
		}

		draw();
		showPoints();
	}

	private void applyNewHeight(double oldHeight, double newHeight, boolean finalize) {
		if (newHeight == 0 || newHeight == oldHeight)
			return;
		double scale = newHeight / oldHeight;

		applyNewHeight(children, scale, finalize);
		if (finalize)
			boundingHeight = newHeight;
	}

	private void applyNewHeight(ArrayList<HousePart> children, double scale, boolean finalize) {
		for (HousePart child : children) {
			if (child instanceof Wall || child instanceof Floor || child instanceof Roof) {
				child.setHeight(child.orgHeight * scale, finalize);
				applyNewHeight(child.getChildren(), scale, finalize);
			}
		}
	}

	@Override
	protected void drawMesh() {
		final boolean drawable = points.size() == 8;
		if (drawable) {
			((Box) mesh).setData(points.get(0), points.get(3).add(0, 0, height, null));
			mesh.updateModelBound();
			boundingMesh.setData(points.get(0), points.get(7));
			boundingMesh.updateModelBound();
			
			// draw wireframe
			final FloatBuffer wireframeVertexBuffer = wireframeMesh.getMeshData().getVertexBuffer();
			wireframeVertexBuffer.rewind();
			drawSideWireframe(wireframeVertexBuffer, 0, 1);
			drawSideWireframe(wireframeVertexBuffer, 1, 3);
			drawSideWireframe(wireframeVertexBuffer, 3, 2);
			drawSideWireframe(wireframeVertexBuffer, 2, 0);

			if (original == null && resizeHouseMode)
				for (HousePart child : children)
					child.draw();
		}
	}

	public void drawSideWireframe(final FloatBuffer wireframeVertexBuffer, final int i, final int j) {
		Vector3 p;
		p = abspoints.get(i);
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = abspoints.get(i);
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf() + (float)height);
		p = abspoints.get(j);
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf() + (float)height);
		p = abspoints.get(j);
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
	}

	private void scanChildrenHeight() {
		if (!isFirstPointInserted())
			return;
		// boundingHeight = 0;
		// for (HousePart child : children) {
		// for (Vector3 p : child.getAbsPoints()) {
		// boundingHeight = Math.max(boundingHeight, p.getZ());
		// }
		// }
		boundingHeight = scanChildrenHeight(this);
		// boundingHeight += 0.5;
		for (int i = 4; i < 8; i++)
			points.get(i).setZ(boundingHeight);
		newBoundingHeight = boundingHeight;
	}

	private double scanChildrenHeight(final HousePart part) {
		double maxHeight = 0;
		if (part instanceof Wall || part instanceof Roof) {
			for (final Vector3 p : part.getAbsPoints())
				maxHeight = Math.max(maxHeight, p.getZ());
		}
		for (final HousePart child : part.children)
			maxHeight = Math.max(maxHeight, scanChildrenHeight(child));
		return maxHeight;
	}

	@Override
	public void flatten(double flattenTime) {
		root.setRotation((new Matrix3().fromAngles(-flattenTime * Math.PI / 2, 0, 0)));
		super.flatten(flattenTime);
	}

	@Override
	protected void computeCenter() {
		center.set(0, 0, 0);
		for (int i = 0; i < points.size() / 2; i++) {
			Vector3 p = points.get(i);
			p = toAbsolute(p);
			pointsRoot.getChild(i).setTranslation(p);
			abspoints.get(i).set(p);
			center.addLocal(p);
		}
		center.multiplyLocal(1.0 / points.size() * 2);
	}

	protected void computeLabelTop(final Vector3 top) {
		top.set(0, 0, ((Box) mesh).getYExtent() + 0.5);
	}

	@Override
	protected void drawAnnotations() {
		int[] order = { 0, 1, 3, 2, 0 };
		int annotCounter = 0;
		for (int i = 0; i < order.length - 1; i++, annotCounter++) {
			final SizeAnnotation annot;
			if (annotCounter < sizeAnnotRoot.getChildren().size())
				annot = (SizeAnnotation) sizeAnnotRoot.getChild(annotCounter);
			else {
				annot = new SizeAnnotation();
				sizeAnnotRoot.attachChild(annot);
			}
			annot.setRange(abspoints.get(order[i]), abspoints.get(order[i + 1]), center, getFaceDirection(), false, original == null ? Align.South : Align.Center, true);
		}

		for (int i = annotCounter; i < sizeAnnotRoot.getChildren().size(); i++)
			sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
	}

	@Override
	public void hidePoints() {
		if (!resizeHouseMode)
			super.hidePoints();
	}

	@Override
	public void setEditPoint(int editPoint) {
		if (!resizeHouseMode && editPoint > 3)
			editPoint -= 4;
		super.setEditPoint(editPoint);
		if (!resizeHouseMode) {
			orgPoints = new ArrayList<Vector3>(4);
			for (int i = 0; i < 4; i++)
				orgPoints.add(points.get(i).clone());
		}
	}

	@Override
	protected String getDefaultTextureFileName() {
		return "foundation.jpg";
	}

}
