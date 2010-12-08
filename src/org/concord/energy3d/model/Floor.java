package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Floor extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.2;
	protected double labelTop;
	private transient ArrayList<PolygonPoint> wallUpperPoints;

	public Floor() {
		super(1, 1, 0.5);
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Floor");
		root.attachChild(mesh);

		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.setModelBound(new BoundingSphere());

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		updateTexture(Scene.getInstance().isTextureEnabled());

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
	}

	public void setPreviewPoint(int x, int y) {
		pick(x, y, Wall.class);
		if (container != null) {
			Vector3 base = container.getPoints().get(0);
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, GRID_SIZE);
			height = Math.max(0, p.getZ() - base.getZ()) + base.getZ();
		}
		draw();
		showPoints();
	}

	private Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		center.set(0, 0, 0);
		double maxY = wallUpperPoints.get(0).getY();
		for (PolygonPoint p : wallUpperPoints) {
			center.addLocal(p.getX(), p.getY(), height);
			p.set(p.getX(), p.getY(), height);
			if (p.getY() > maxY)
				maxY = p.getY();
		}
		center.multiplyLocal(1.0 / wallUpperPoints.size());
		labelTop = (maxY - center.getY());
		points.get(0).set(center);
		return new Polygon(wallUpperPoints);
	}

	private void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
			ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
			ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
			ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
			ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 0.1, 0);
			mesh.getMeshData().updateVertexCount();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public void updateTexture() {
//		if (textureEnabled) {
//			final TextureState ts = new TextureState();
//			ts.setTexture(TextureManager.load(textureFileName, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
//			mesh.setRenderState(ts);
//			mesh.setDefaultColor(ColorRGBA.WHITE);
//		} else {
//			mesh.clearRenderState(StateType.Texture);
//			mesh.setDefaultColor(defaultColor);
//		}
//	}

	protected void computeAbsPoints() {
	}

	protected void computeCenter() {
	}

	protected void drawMesh() {
		if (container == null) {
//			resetToZero(mesh.getMeshData().getVertexBuffer());
			mesh.getSceneHints().setCullHint(CullHint.Always);
			hidePoints();
			return;
		}
		
		mesh.getSceneHints().setCullHint(CullHint.Inherit);

		wallUpperPoints = exploreWallNeighbors((Wall) container);

		fillMeshWithPolygon(mesh, makePolygon(wallUpperPoints));

		for (int i = 0; i < points.size(); i++)
			pointsRoot.getChild(i).setTranslation(points.get(i));

		mesh.updateModelBound();
	}

//	private void resetToZero(final FloatBuffer buff) {
//		buff.rewind();
//		while (buff.hasRemaining())
//			buff.put(0);
//	}


	protected ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
		center.set(0, 0, 0);
		final ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		startWall.visitNeighbors(new WallVisitor() {
			public void visit(Wall currentWall, Snap prev, Snap next) {
				int pointIndex = 0;
				if (next != null)
					pointIndex = next.getSnapPointIndexOf(currentWall);
				pointIndex = pointIndex + 1;
				final Vector3 p1 = currentWall.getPoints().get(pointIndex == 1 ? 3 : 1);
				final Vector3 p2 = currentWall.getPoints().get(pointIndex);
				addPointToPolygon(poly, p1, center);
				addPointToPolygon(poly, p2, center);
			}

		});

		center.multiplyLocal(1.0 / poly.size());
		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);

		return poly;
	}

	private void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p, Vector3 center) {
		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint)) {
			poly.add(polygonPoint);
			center.addLocal(p);
		}
	}

	public ReadOnlyVector3 getFaceDirection() {
		return Vector3.UNIT_Z;
	}

	protected void drawAnnotations() {
		if (container == null)
			return;
		int annotCounter = 0;

		for (int i = 0; i < wallUpperPoints.size(); i++) {
			PolygonPoint p = wallUpperPoints.get(i);
			Vector3 a = new Vector3(p.getX(), p.getY(), p.getZ());
			p = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
			Vector3 b = new Vector3(p.getX(), p.getY(), p.getZ());
			fetchSizeAnnot(annotCounter++).setRange(a, b, center, getFaceDirection(), original == null, Align.Center, true);
		}

		for (int i = annotCounter; i < sizeAnnotRoot.getChildren().size(); i++)
			sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
	}
	
	protected String getDefaultTextureFileName() {
		return "floor.jpg";
	}
	
	protected void flatten() {
		root.setRotation((new Matrix3().fromAngles(-flattenTime * Math.PI / 2, 0, 0)));
		super.flatten();
	}	
}