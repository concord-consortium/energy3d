package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.TextureManager;

public abstract class Roof extends HousePart {
	static private final long serialVersionUID = 1L;
	static protected final double GRID_SIZE = 0.5;
	transient protected Mesh bottomMesh;
	transient protected Node flattenedMeshesRoot;
	transient private ArrayList<PolygonPoint> wallUpperPoints;
	transient private Map<Mesh, Vector3> orgCenters;

	public Roof(int numOfDrawPoints, int numOfEditPoints, double height) {
		super(numOfDrawPoints, numOfEditPoints, height);
	}

	protected void init() {
		super.init();
		flattenedMeshesRoot = new Node("Roof Meshes Root");
		root.attachChild(flattenedMeshesRoot);

		mesh = new Mesh("Roof");
		bottomMesh = new Mesh("Roof (bottom)");

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		bottomMesh.setRenderState(ms);

		updateTextureAndColor(Scene.getInstance().isTextureEnabled());

		bottomMesh.setUserData(new UserData(this));
	}

	protected void computeAbsPoints() {
	}

	protected void computeCenter() {
	}

	protected void drawMesh() {
		try {
			if (container == null) {
				flattenedMeshesRoot.getSceneHints().setCullHint(CullHint.Always);
				bottomMesh.getSceneHints().setCullHint(CullHint.Always);
				hidePoints();
				return;
			}
			flattenedMeshesRoot.getSceneHints().setCullHint(CullHint.Inherit);
			bottomMesh.getSceneHints().setCullHint(CullHint.Inherit);

			wallUpperPoints = exploreWallNeighbors((Wall) container);

			fillMeshWithPolygon(bottomMesh, new Polygon(wallUpperPoints));
			if (!root.hasChild(bottomMesh))
				root.attachChild(bottomMesh);
			fillMeshWithPolygon(mesh, makePolygon(wallUpperPoints));
			
			// create roof parts
			int meshIndex = 0;
			MeshLib.groupByPlanner(mesh, flattenedMeshesRoot);
			for (final Spatial child : flattenedMeshesRoot.getChildren())
				child.setUserData(new UserData(this, meshIndex++));

			for (int i = 0; i < points.size(); i++) {
				Vector3 p = points.get(i);
				pointsRoot.getChild(i).setTranslation(p);
			}

			if (bottomMesh != null)
				bottomMesh.updateModelBound();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		Poly2Tri.triangulate(polygon);
		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);

		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, 0);
		mesh.getMeshData().updateVertexCount();
	}

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

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		return new Polygon(wallUpperPoints);
	}

	public void flatten(double flattenTime) {
		for (Spatial child : getFlattenedMeshesRoot().getChildren())
			flattenQuadTriangle((Mesh) child, flattenTime);
		mesh.updateModelBound();
		if (bottomMesh != null)
			bottomMesh.getSceneHints().setCullHint(CullHint.Always);
	}

	private void flattenQuadTriangle(final Mesh mesh, final double flattenTime) {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		final Vector3 p1 = new Vector3(buf.get(), buf.get(), buf.get());
		final Vector3 p2 = new Vector3(buf.get(), buf.get(), buf.get());
		final Vector3 p3 = new Vector3(buf.get(), buf.get(), buf.get());

		final Vector3 v = new Vector3(p3).subtractLocal(p1);
		final Vector3 normal = new Vector3(p2).subtractLocal(p1).crossLocal(v).normalizeLocal();
		final double angle = normal.smallestAngleBetween(Vector3.UNIT_Y);
		final Vector3 rotAxis = normal.cross(Vector3.UNIT_Y, null);
		mesh.setRotation(new Matrix3().fromAngleAxis(flattenTime * angle, rotAxis));

		if (orgCenters == null)
			orgCenters = new HashMap<Mesh, Vector3>();
		Vector3 orgCenter = orgCenters.get(mesh);
		if (orgCenter == null) {
			final Matrix3 m = new Matrix3().fromAngleAxis(angle, rotAxis);
			m.applyPost(p1, p1);
			m.applyPost(p2, p2);
			m.applyPost(p3, p3);
			orgCenter = new Vector3(p1).addLocal(p2).addLocal(p3);
			while (buf.hasRemaining())
				orgCenter.addLocal(m.applyPost(new Vector3(buf.get(), buf.get(), buf.get()), p1));
			orgCenter.divideLocal(buf.capacity() / 3);
			orgCenters.put(mesh, orgCenter);
		}
		final Vector3 targetPrintCenter = ((UserData) mesh.getUserData()).getPrintCenter();
		mesh.setTranslation(targetPrintCenter.subtract(orgCenter, null).multiplyLocal(flattenTime));
	}

	protected void drawAnnotations() {
		if (container == null)
			return;
		int annotCounter = 0, angleAnnotCounter = 0;

		if (original == null) {
			for (int i = 0; i < wallUpperPoints.size(); i++) {
				PolygonPoint p = wallUpperPoints.get(i);
				Vector3 a = new Vector3(p.getX(), p.getY(), p.getZ());
				p = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
				Vector3 b = new Vector3(p.getX(), p.getY(), p.getZ());
				fetchSizeAnnot(annotCounter++).setRange(a, b, center, getFaceDirection(), original == null, Align.Center, true);
			}
		} else {
			final Vector3 p1 = new Vector3();
			final Vector3 p2 = new Vector3();
			final Vector3 p3 = new Vector3();
			for (Spatial mesh : flattenedMeshesRoot.getChildren()) {
				final FloatBuffer vertexBuffer = ((Mesh) mesh).getMeshData().getVertexBuffer();
				float pos = 0;
				for (int i = 0; i < vertexBuffer.capacity() / 9; i++) {
					pos += 0.5;
					final int xPos = i * 9;
					vertexBuffer.position(xPos);
					p1.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
					p2.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
					p3.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
					mesh.getTransform().applyForward(p1);
					mesh.getTransform().applyForward(p2);
					mesh.getTransform().applyForward(p3);

					// Size annotation
					fetchSizeAnnot(annotCounter++).setRange(p1, p2, center, Vector3.UNIT_Y, original == null, Align.Center, false);
					fetchSizeAnnot(annotCounter++).setRange(p2, p3, center, Vector3.UNIT_Y, original == null, Align.Center, false);
					fetchSizeAnnot(annotCounter++).setRange(p3, p1, center, Vector3.UNIT_Y, original == null, Align.Center, false);

					// Angle annotations
					fetchAngleAnnot(angleAnnotCounter++).setRange(p1, p2, p3);
					fetchAngleAnnot(angleAnnotCounter++).setRange(p2, p3, p1);
					fetchAngleAnnot(angleAnnotCounter++).setRange(p3, p1, p2);
				}
			}
		}

		for (int i = annotCounter; i < sizeAnnotRoot.getChildren().size(); i++)
			sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
	}

	public int drawLabels(int printSequence) {
		final Vector3 p = new Vector3();
		final Vector3 center = new Vector3();
		int triangle = 0;
		for (Spatial roofGroup : flattenedMeshesRoot.getChildren()) {
			final FloatBuffer buf = ((Mesh) roofGroup).getMeshData().getVertexBuffer();
			buf.rewind();
			double height = Double.NEGATIVE_INFINITY;
			center.set(0, 0, 0);
			while (buf.hasRemaining()) {
				p.set(buf.get(), buf.get(), buf.get());
				roofGroup.getTransform().applyForward(p);
				height = Math.max(p.getZ(), height);
				center.addLocal(p);
			}
			center.divideLocal(buf.capacity() / 3);
			if (original == null)
				center.setZ(height);
			else
				center.addLocal(0, -0.01, 0);

			final String text = "(" + (printSequence++ + 1) + ")";
			final BMText label = fetchBMText(text, triangle++);
			label.setTranslation(center);
		}
		return printSequence;
	}

	public void updateTextureAndColor(final boolean textureEnabled) {
		if (textureEnabled) {
			final TextureState ts = new TextureState();
			ts.setTexture(TextureManager.load(textureFileName, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
			mesh.setRenderState(ts);
			mesh.setDefaultColor(ColorRGBA.WHITE);

			if (flattenedMeshesRoot != null) {
				flattenedMeshesRoot.setRenderState(ts);
				for (Spatial s : flattenedMeshesRoot.getChildren()) {
					Mesh mesh = (Mesh) s;
					mesh.setDefaultColor(ColorRGBA.WHITE);
				}
			}
			if (bottomMesh != null) {
				bottomMesh.setRenderState(ts);
				bottomMesh.setDefaultColor(ColorRGBA.WHITE);
			}
		} else {
			mesh.clearRenderState(StateType.Texture);
			mesh.setDefaultColor(defaultColor);
			if (flattenedMeshesRoot != null) {
				flattenedMeshesRoot.clearRenderState(StateType.Texture);
				for (Spatial s : flattenedMeshesRoot.getChildren()) {
					Mesh mesh = (Mesh) s;
					mesh.setDefaultColor(defaultColor);
				}
			}
			if (bottomMesh != null) {
				bottomMesh.clearRenderState(StateType.Texture);
				bottomMesh.setDefaultColor(defaultColor);
			}
		}
	}

	public Node getFlattenedMeshesRoot() {
		return flattenedMeshesRoot;
	}

	protected String getDefaultTextureFileName() {
		return "roof.jpg";
	}

}
