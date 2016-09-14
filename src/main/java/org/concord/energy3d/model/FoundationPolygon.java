package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

public class FoundationPolygon extends HousePart {
	private static final long serialVersionUID = 1L;

	public FoundationPolygon(final Foundation foundation) {
		super(1, 8, 0, true);
		this.container = foundation;
		final double h = foundation.getHeight();
		points.get(0).set(0.1, 0.1, h);
		points.get(1).set(0.9, 0.1, h);
		points.get(2).set(0.9, 0.9, h);
		points.get(3).set(0.1, 0.9, h);
	}

	@Override
	protected void init() {
		super.init();
		final Line line = new Line("Foundation Polygon");
		line.setLineWidth(3);
		line.getMeshData().setIndexMode(IndexMode.LineLoop);
		line.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		line.setModelBound(new BoundingBox());
		root.attachChild(line);
		mesh = line;
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart pick = SelectUtil.pickPart(x, y, container);
		Vector3 p;
		if (pick != null) {
			p = pick.getPoint().clone();
		} else {
			p = points.get(editPointIndex).clone();
		}

		final Vector3 relativeP = toRelative(p);
		final int n = points.size() / 2;
		if (editPointIndex < n) {
			points.get(editPointIndex).set(relativeP);
		} else {
			SelectUtil.getCurrentEditPointMesh().setDefaultColor(ColorRGBA.WHITE);
			editPointIndex -= n;
			final Mesh editPointShape = getEditPointShape(editPointIndex);
			SelectUtil.setCurrentEditPointMesh(editPointShape);
			editPointShape.setDefaultColor(ColorRGBA.RED);
			points.add(editPointIndex, relativeP);
			points.add(new Vector3());
			updateEditShapes();
			setEditPointsVisible(true);
		}
		draw();
	}

	@Override
	protected void drawMesh() {
		final int n = points.size() / 2;
		final FloatBuffer buf = BufferUtils.createVector3Buffer(n);
		mesh.getMeshData().setVertexBuffer(buf);
		for (int i = 0; i < n; i++) {
			final Vector3 p = getAbsPoint(i);
			BufferUtils.setInBuffer(p, buf, i);
			points.get(n + i).set(points.get(i)).addLocal(points.get(i == 0 ? n - 1 : i - 1)).multiplyLocal(0.5);
		}
	}

	@Override
	protected void computeArea() {

	}

	@Override
	public boolean isCopyable() {
		return false;
	}

	@Override
	protected String getTextureFileName() {
		return null;
	}

	@Override
	public void updateTextureAndColor() {

	}

}
