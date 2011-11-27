package org.concord.energy3d.model;

import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Floor extends HousePart {
	private static final long serialVersionUID = 1L;
//	private static final double getGridSize() = 0.2;
	private transient ArrayList<PolygonPoint> wallUpperPoints;

	public Floor() {
		super(1, 1, 0.5);
	}

	protected void init() {
		super.init();
		relativeToHorizontal = true;
		mesh = new Mesh("Floor");
		root.attachChild(mesh);

		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.setModelBound(new BoundingBox());

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		updateTextureAndColor(Scene.getInstance().isTextureEnabled());

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
	}

	public void setPreviewPoint(int x, int y) {
		pickContainer(x, y, Wall.class);
		if (container != null) {
			Vector3 base = container.getAbsPoint(0);
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, getGridSize());
			height = Math.max(0, p.getZ() - base.getZ()) + base.getZ();
		}
		draw();
		setEditPointsVisible(true);
	}

	private Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		double maxY = wallUpperPoints.get(0).getY();
		for (PolygonPoint p : wallUpperPoints) {
			p.set(p.getX(), p.getY(), height);
			if (p.getY() > maxY)
				maxY = p.getY();
		}
		points.get(0).set(toRelative(getCenter(), container.getContainer()));
		return new Polygon(wallUpperPoints);
	}

	private void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		Poly2Tri.triangulate(polygon);
		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, new TPoint(0, 0, 0), new TPoint(1, 0, 0), new TPoint(0, 1, 0));
		mesh.getMeshData().updateVertexCount();
		mesh.updateModelBound();
		root.updateWorldBound(true);
	}

	protected void computeAbsPoints() {
	}

	protected void computeCenter() {
	}

	protected void drawMesh() {
		try {
			if (container == null) {
				mesh.getSceneHints().setCullHint(CullHint.Always);
				setEditPointsVisible(false);
				return;
			}
			mesh.getSceneHints().setCullHint(CullHint.Inherit);
			wallUpperPoints = exploreWallNeighbors((Wall) container);
			fillMeshWithPolygon(mesh, makePolygon(wallUpperPoints));
			for (int i = 0; i < points.size(); i++)
				pointsRoot.getChild(i).setTranslation(points.get(i));
			mesh.updateModelBound();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
		final ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		startWall.visitNeighbors(new WallVisitor() {
			public void visit(Wall currentWall, Snap prev, Snap next) {
				int pointIndex = 0;
				if (next != null)
					pointIndex = next.getSnapPointIndexOf(currentWall);
				pointIndex = pointIndex + 1;
				final Vector3 p1 = currentWall.getAbsPoint(pointIndex == 1 ? 3 : 1);
				final Vector3 p2 = currentWall.getAbsPoint(pointIndex);
				addPointToPolygon(poly, p1);
				addPointToPolygon(poly, p2);
			}

		});

		return poly;
	}

	private void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p) {
		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint))
			poly.add(polygonPoint);
	}

	public void drawAnnotations() {
		if (container == null)
			return;
		int annotCounter = 0;

		for (int i = 0; i < wallUpperPoints.size(); i++) {
			PolygonPoint p = wallUpperPoints.get(i);
			Vector3 a = new Vector3(p.getX(), p.getY(), p.getZ());
			p = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
			Vector3 b = new Vector3(p.getX(), p.getY(), p.getZ());
			fetchSizeAnnot(annotCounter++).setRange(a, b, getCenter(), getFaceDirection(), original == null, Align.Center, true, false, Scene.isDrawAnnotationsInside());
		}
	}

	protected String getDefaultTextureFileName() {
		return "floor.jpg";
	}

	public void flatten(double flattenTime) {
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
		root.updateWorldTransform(true);
		super.flatten(flattenTime);
	}

	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index), container == null ? null : container.getContainer());
	}

	@Override
	public void setOriginal(final HousePart original) {
		wallUpperPoints = ((Floor) original).wallUpperPoints;
		super.setOriginal(original);
	}
}
