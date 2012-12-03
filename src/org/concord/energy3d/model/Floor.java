package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.point.TPoint;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Floor extends HousePart {
	private static final long serialVersionUID = 1L;
	private transient ArrayList<PolygonPoint> wallUpperPoints;
	private transient Mesh wireframeMesh;

	public Floor() {
		super(1, 1, 5.0);
	}

	@Override
	protected void init() {
		super.init();
		relativeToHorizontal = true;
		mesh = new Mesh("Floor");
		root.attachChild(mesh);

		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
//		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setModelBound(new BoundingBox());

		wireframeMesh = new Line("Floor (Wireframe)");
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(wireframeMesh);
//		root.attachChild(wireframeMesh);

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		updateTextureAndColor();

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		pickContainer(x, y, Wall.class);
		if (container != null) {
			final ReadOnlyVector3 base = getCenter();
			final Vector3 p = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
			snapToGrid(p, base, getGridSize());
			final double zMin = container.getAbsPoint(0).getZ() + 0.01;
			final double zmax = container.getAbsPoint(1).getZ();
			height = Math.min(zmax, Math.max(zMin, p.getZ()));
		}
		draw();
		setEditPointsVisible(container != null);
	}

	private Polygon makePolygon(final ArrayList<PolygonPoint> wallUpperPoints) {
		double maxY = wallUpperPoints.get(0).getY();
		for (final PolygonPoint p : wallUpperPoints) {
			p.set(p.getX(), p.getY(), height);
			if (p.getY() > maxY)
				maxY = p.getY();
		}
		points.get(0).set(toRelative(getCenter(), container.getContainer()));
		return new Polygon(wallUpperPoints);
	}

	@Override
	protected void drawMesh() {
		if (container == null) {
			mesh.getSceneHints().setCullHint(CullHint.Always);
			return;
		}
		mesh.getSceneHints().setCullHint(CullHint.Inherit);
		wallUpperPoints = exploreWallNeighbors((Wall) container);
		final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 2.0 : 10.0;
		MeshLib.fillMeshWithPolygon(mesh, makePolygon(wallUpperPoints), null, true, new TPoint(0, 0, 0), new TPoint(scale, 0, 0), new TPoint(0, scale, 0));
		drawWireframe();
		updateEditShapes();
	}

	protected ArrayList<PolygonPoint> exploreWallNeighbors(final Wall startWall) {
		final ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		startWall.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				int pointIndex = 0;
				if (next != null)
					pointIndex = next.getSnapPointIndexOf(currentWall);
				pointIndex = pointIndex + 1;
				final ReadOnlyVector3 p1 = currentWall.getAbsPoint(pointIndex == 1 ? 3 : 1);
				final ReadOnlyVector3 p2 = currentWall.getAbsPoint(pointIndex);
				addPointToPolygon(poly, p1);
				addPointToPolygon(poly, p2);
			}
		});

		return poly;
	}

	private void addPointToPolygon(final ArrayList<PolygonPoint> poly, final ReadOnlyVector3 p) {
		final PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint))
			poly.add(polygonPoint);
	}

	@Override
	public void drawAnnotations() {
		if (container == null)
			return;
		int annotCounter = 0;

		for (int i = 0; i < wallUpperPoints.size(); i++) {
			PolygonPoint p = wallUpperPoints.get(i);
			final Vector3 a = new Vector3(p.getX(), p.getY(), p.getZ());
			p = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
			final Vector3 b = new Vector3(p.getX(), p.getY(), p.getZ());
			final SizeAnnotation sizeAnnot = fetchSizeAnnot(annotCounter++);
			sizeAnnot.setRange(a, b, getCenter(), getFaceDirection(), original == null, Align.Center, true, false, Scene.isDrawAnnotationsInside());
			sizeAnnot.setLineWidth(original == null ? 1f : 2f);
		}
	}

	protected void drawWireframe() {
		if (container == null)
			return;

		final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeOutline(mesh.getMeshData().getVertexBuffer());
		final int totalVertices = convexHull.size();

		final FloatBuffer buf;
		if (wireframeMesh.getMeshData().getVertexBuffer().capacity() >= totalVertices * 2 * 3) {
			buf = wireframeMesh.getMeshData().getVertexBuffer();
			buf.limit(buf.capacity());
			buf.rewind();
		} else {
			buf = BufferUtils.createVector3Buffer(totalVertices * 2);
			wireframeMesh.getMeshData().setVertexBuffer(buf);
		}

		for (int i = 0; i < convexHull.size(); i++) {
			final ReadOnlyVector3 p1 = convexHull.get(i);
			final ReadOnlyVector3 p2 = convexHull.get((i + 1) % convexHull.size());

			buf.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
			buf.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		}
		buf.limit(buf.position());
		wireframeMesh.getMeshData().updateVertexCount();
		wireframeMesh.updateModelBound();
		wireframeMesh.setTranslation(getFaceDirection().multiply(0.001, null));
	}

	@Override
	protected String getTextureFileName() {
		return Scene.getInstance().getTextureMode() == TextureMode.Simple ? "floor.png" : "floor.jpg";
	}

	@Override
	public void flatten(final double flattenTime) {
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
		root.updateWorldTransform(true);
		super.flatten(flattenTime);
	}

	@Override
	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index), container == null ? null : container.getContainer());
	}

	@Override
	public void setOriginal(final HousePart original) {
		wallUpperPoints = ((Floor) original).wallUpperPoints;
		root.detachChild(wireframeMesh);
		wireframeMesh = ((Floor) original).wireframeMesh.makeCopy(true);
		((Line) wireframeMesh).setLineWidth(printWireframeThickness);
		root.attachChild(wireframeMesh);
		super.setOriginal(original);
	}

	@Override
	public boolean isDrawable() {
		return container != null;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.getInstance().getFloorColor());
	}
}
