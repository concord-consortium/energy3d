package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
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
	private transient Box boundingMesh;
	private transient Mesh wireframeMesh;
	private transient ArrayList<Vector3> orgPoints;
	private transient double newBoundingHeight;
	private transient double boundingHeight;
	private transient boolean resizeHouseMode = false;

	public Foundation() {
		super(2, 8, 0.1);
	}

	@Override
	protected void init() {
		super.init();
		resizeHouseMode = false;

		mesh = new Box("Foundation", new Vector3(), new Vector3());
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);
		mesh.setModelBound(new BoundingBox());
		updateTextureAndColor(Scene.getInstance().isTextureEnabled());
		root.attachChild(mesh);

		boundingMesh = new Box("Foundation (Bounding)", new Vector3(), new Vector3());
		boundingMesh.setRenderState(new WireframeState());
		boundingMesh.setModelBound(new BoundingBox());

		wireframeMesh = new Mesh("Foundation (wireframe)");
		wireframeMesh.getMeshData().setIndexMode(IndexMode.Quads);
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(16));
		wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		wireframeMesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wireframeMesh.getSceneHints().setCastsShadows(false);
		wireframeMesh.setRenderState(new WireframeState());
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setModelBound(new BoundingBox());
		root.attachChild(wireframeMesh);

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		boundingMesh.setUserData(userData);

		// scanChildrenHeight(); // to fix bug with resizing height instead of width when moving edit point of platform right after loading the model

		setLabelOffset(-0.11);
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
			computeEditPointScale(i);
			if (!resizeHouseMode && i >= 4)
				pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
			else
				pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Inherit);
		}
	}

	@Override
	public void complete() {
		super.complete();
		newBoundingHeight = points.get(4).getZ() - height; // problem?
		applyNewHeight(boundingHeight, newBoundingHeight, true);
		if (!resizeHouseMode) {
			final double dx = Math.abs(points.get(2).getX() - points.get(0).getX());
			final double dxOrg = Math.abs(orgPoints.get(2).getX() - orgPoints.get(0).getX());
			final double ratioX = dx / dxOrg;
			final double dy = Math.abs(points.get(1).getY() - points.get(0).getY());
			final double dyOrg = Math.abs(orgPoints.get(1).getY() - orgPoints.get(0).getY());
			final double ratioY = dy / dyOrg;
			final ArrayList<HousePart> roofs = new ArrayList<HousePart>();
			for (final HousePart child : children) {
				reverseFoundationResizeEffect(child, dx, dxOrg, ratioX, dy, dyOrg, ratioY);
				if (child instanceof Wall) {
					final HousePart roof = ((Wall) child).getRoof();
					if (roof != null && !roofs.contains(roof)) {
						reverseFoundationResizeEffect(roof, dx, dxOrg, ratioX, dy, dyOrg, ratioY);
						roofs.add(roof);
					}
				}
			}
			orgPoints = null;
		}
	}

	private void reverseFoundationResizeEffect(final HousePart child, final double dx, final double dxOrg, final double ratioX, final double dy, final double dyOrg, final double ratioY) {
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
				final int lower = editPointIndex - 4;
				final Vector3 base = getAbsPoint(lower);
				Vector3 closestPoint = closestPoint(base, Vector3.UNIT_Z, x, y);
				closestPoint = grid(closestPoint, GRID_SIZE);
				newBoundingHeight = Math.max(0, closestPoint.getZ() - base.getZ());
				applyNewHeight(boundingHeight, newBoundingHeight, false);
			}
			for (int i = 0; i < 4; i++)
				points.get(i + 4).set(points.get(i)).setZ(newBoundingHeight + height);
		}

		if (resizeHouseMode)
			Scene.getInstance().redrawAll();
		else
			draw();
		showPoints();
	}

	private void applyNewHeight(double orgHeight, double newHeight, boolean finalize) {
		if (newHeight == 0 || newHeight == orgHeight)
			return;
		double scale = newHeight / orgHeight;

		applyNewHeight(children, scale, finalize);
		if (finalize)
			this.boundingHeight = newHeight;
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
		if (boundingHeight == 0)
			scanChildrenHeight();
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
			wireframeMesh.updateModelBound();
		}
	}

	public void drawSideWireframe(final FloatBuffer wireframeVertexBuffer, final int i, final int j) {
		Vector3 p;
		p = getAbsPoint(i);
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf() + (float) height);
		p = getAbsPoint(j);
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf() + (float) height);
		wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
	}

	private void scanChildrenHeight() {
		if (!isFirstPointInserted())
			return;
		boundingHeight = scanChildrenHeight(this) - height;
		for (int i = 4; i < 8; i++) {
			points.get(i).setZ(boundingHeight + height);
		}
		newBoundingHeight = boundingHeight;
		updateEditShapes();
	}

	private double scanChildrenHeight(final HousePart part) {
		double maxHeight = 0;
		if (part instanceof Wall || part instanceof Roof) {
			for (int i = 0; i < part.points.size(); i++) {
				final ReadOnlyVector3 p = part.getAbsPoint(i);
				maxHeight = Math.max(maxHeight, p.getZ());
			}
		}
		for (final HousePart child : part.children)
			maxHeight = Math.max(maxHeight, scanChildrenHeight(child));
		return maxHeight;
	}

	@Override
	public void flattenInit() {
		super.flattenInit();
		flattenCenter.setY(0);
	}

	@Override
	public void flatten(double flattenTime) {
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
		super.flatten(flattenTime);
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
			annot.setRange(getAbsPoint(order[i]), getAbsPoint(order[i + 1]), getCenter(), getFaceDirection(), false, Align.Center, true, true, false);
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
			prepareForNotResizing();
		}
	}

	public void prepareForNotResizing() {
		orgPoints = new ArrayList<Vector3>(4);
		for (int i = 0; i < 4; i++)
			orgPoints.add(points.get(i).clone());
	}

	@Override
	protected String getDefaultTextureFileName() {
		return "foundation.jpg";
	}
}
