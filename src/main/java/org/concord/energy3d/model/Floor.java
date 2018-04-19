package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.PolygonWithHoles;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.point.TPoint;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Floor extends HousePart {

	public static final int TEXTURE_01 = 1;
	public static final int SOLID = 0;
	public static final int TRANSPARENT = 1;
	private static final long serialVersionUID = 1L;
	private static final OffsetState offsetState = new OffsetState();
	private transient List<PolygonPoint> wallUpperPoints;
	private transient List<ReadOnlyVector3> wallUpperVectors;
	private transient Line outlineMesh;
	private int type = SOLID;
	private int textureType = 0;

	static {
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(10f);
		offsetState.setUnits(1f);
	}

	public Floor() {
		super(1, 1, 5);
	}

	@Override
	protected void init() {
		super.init();

		mesh = new Mesh("Floor");
		root.attachChild(mesh);

		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.setModelBound(new BoundingBox());
		mesh.setRenderState(offsetState);

		outlineMesh = new Line("Floor (Outline)");
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(outlineMesh);
		/* no need to attach because floor outline is only need in print preview */

		updateTextureAndColor();

		mesh.setUserData(new UserData(this));
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final Foundation foundation = getTopContainer();
		if (foundation != null && foundation.getLockEdit()) {
			return;
		}
		pickContainer(x, y, Wall.class);
		if (container != null) {
			final ReadOnlyVector3 base = getCenter();
			final Vector3 p = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
			if (p == null) {
				return;
			}
			snapToGrid(p, base, getGridSize());
			final double zMin = container.getAbsPoint(0).getZ() + 0.5;
			final double zmax = container.getAbsPoint(1).getZ();
			height = Math.min(zmax, Math.max(zMin, p.getZ()));
		}
		draw();
		setEditPointsVisible(container != null);
		final Foundation f = getTopContainer();
		if (f != null) {
			f.draw();
			f.drawChildren(); // need to redraw the walls when the floor is part of a balcony
		}
	}

	private PolygonWithHoles makePolygon(final List<PolygonPoint> wallUpperPoints) {
		for (final PolygonPoint p : wallUpperPoints) {
			p.set(p.getX(), p.getY(), height);
		}
		return new PolygonWithHoles(wallUpperPoints);
	}

	@Override
	protected void drawMesh() {
		if (container != null) {
			if (wallUpperVectors == null) {
				wallUpperVectors = new ArrayList<ReadOnlyVector3>();
			} else {
				wallUpperVectors.clear();
			}
			wallUpperPoints = exploreWallNeighbors((Wall) container);
		}
		if (!isDrawable()) {
			mesh.getSceneHints().setCullHint(CullHint.Always);
			return;
		}
		switch (type) {
		case TRANSPARENT:
			mesh.getSceneHints().setCullHint(CullHint.Always);
			break;
		default:
			mesh.getSceneHints().setCullHint(CullHint.Inherit);
		}
		final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 2.0 : 10.0;
		MeshLib.fillMeshWithPolygon(mesh, makePolygon(wallUpperPoints), null, true, new TPoint(0, 0, 0), new TPoint(scale, 0, 0), new TPoint(0, scale, 0), false);
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		drawOutline();
		points.get(0).set(toRelative(getCenter()));
		updateEditShapes();
	}

	protected boolean insideWallsPolygon(final Vector3 p) {
		return Util.insidePolygon(p, wallUpperVectors);
	}

	protected ArrayList<PolygonPoint> exploreWallNeighbors(final Wall startWall) {
		final ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		startWall.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				int pointIndex = 0;
				if (next != null) {
					pointIndex = next.getSnapPointIndexOf(currentWall);
				}
				pointIndex = pointIndex + 1;
				final ReadOnlyVector3 p1 = currentWall.getAbsPoint(pointIndex == 1 ? 3 : 1);
				final ReadOnlyVector3 p2 = currentWall.getAbsPoint(pointIndex);
				addPointToPolygon(poly, p1);
				addPointToPolygon(poly, p2);
				wallUpperVectors.add(p1);
				wallUpperVectors.add(p2);
			}
		});

		return poly;
	}

	private void addPointToPolygon(final ArrayList<PolygonPoint> poly, final ReadOnlyVector3 p) {
		final PolygonPoint polygonPoint = new PolygonPoint(Util.round(p.getX()), Util.round(p.getY()), Util.round(p.getZ()));
		if (!poly.contains(polygonPoint)) {
			poly.add(polygonPoint);
		}
	}

	@Override
	public void drawAnnotations() {
		if (container == null) {
			return;
		}
		int annotCounter = 0;
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			PolygonPoint p = wallUpperPoints.get(i);
			final Vector3 a = new Vector3(p.getX(), p.getY(), p.getZ());
			p = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
			final Vector3 b = new Vector3(p.getX(), p.getY(), p.getZ());
			final SizeAnnotation sizeAnnot = fetchSizeAnnot(annotCounter++);
			sizeAnnot.setRange(a, b, getCenter(), getNormal(), original == null, Align.Center, true, false, Scene.isDrawAnnotationsInside());
			sizeAnnot.setLineWidth(original == null ? 1f : 2f);
		}
	}

	protected void drawOutline() {
		if (container == null) {
			return;
		}

		final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeOutline(mesh.getMeshData().getVertexBuffer());
		final int totalVertices = convexHull.size();

		final FloatBuffer buf;
		if (outlineMesh.getMeshData().getVertexBuffer().capacity() >= totalVertices * 2 * 3) {
			buf = outlineMesh.getMeshData().getVertexBuffer();
			buf.limit(buf.capacity());
			buf.rewind();
		} else {
			buf = BufferUtils.createVector3Buffer(totalVertices * 2);
			outlineMesh.getMeshData().setVertexBuffer(buf);
		}

		for (int i = 0; i < convexHull.size(); i++) {
			final ReadOnlyVector3 p1 = convexHull.get(i);
			final ReadOnlyVector3 p2 = convexHull.get((i + 1) % convexHull.size());

			buf.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
			buf.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		}
		buf.limit(buf.position());
		outlineMesh.getMeshData().updateVertexCount();
		outlineMesh.updateModelBound();
		outlineMesh.setTranslation(getNormal().multiply(0.001, null));
	}

	@Override
	protected String getTextureFileName() {
		final TextureMode t = Scene.getInstance().getTextureMode();
		if (t == TextureMode.None) {
			return null;
		}
		if (t == TextureMode.Simple) {
			return "floor.png";
		}
		switch (textureType) {
		case TEXTURE_01:
			return "floor_01.png";
		}
		return null;
	}

	public void setTextureType(final int textureType) {
		this.textureType = textureType;
	}

	public int getTextureType() {
		return textureType;
	}

	@Override
	public void flatten(final double flattenTime) {
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
		root.updateWorldTransform(true);
		super.flatten(flattenTime);
	}

	@Override
	public void setOriginal(final HousePart original) {
		wallUpperPoints = ((Floor) original).wallUpperPoints;
		root.detachChild(outlineMesh);
		outlineMesh = ((Floor) original).outlineMesh.makeCopy(true);
		outlineMesh.setLineWidth(printOutlineThickness);
		root.attachChild(outlineMesh);
		super.setOriginal(original);
	}

	@Override
	public boolean isDrawable() {
		return container != null && (wallUpperPoints == null || wallUpperPoints.size() >= 3);
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, getColor() == null ? Scene.getInstance().getFloorColor() : getColor());
	}

	@Override
	protected HousePart getContainerRelative() {
		return getTopContainer();
	}

	@Override
	public void drawHeatFlux() {
	}

	@Override
	protected void computeArea() {
		final Building b = new Building(getTopContainer());
		if (b.areWallsAcceptable()) {
			b.calculate();
			area = b.getArea();
		} else {
			area = -1; // return a negative number to indicate problems
		}
	}

	@Override
	public boolean isCopyable() {
		return false;
	}

	public void setType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
