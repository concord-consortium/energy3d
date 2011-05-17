package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public abstract class Roof extends HousePart {
	static private final long serialVersionUID = 1L;
	static protected final double GRID_SIZE = 0.5;
	static private final double OVERHANG_LENGHT = 0; //0.3;
	transient protected Mesh bottomMesh;
	transient protected Node flattenedMeshesRoot;
	transient private ArrayList<PolygonPoint> wallUpperPoints;
	transient private ArrayList<ReadOnlyVector3> wallNormals;
	transient private Map<Mesh, Vector3> orgCenters;
	transient private Line wireframeMesh;
	transient private ArrayList<Vector3[]> gableBases;
	transient private ArrayList<Vector3> gablePoints;
	transient private ArrayList<Wall> walls;
//	transient private ArrayList<Vector3> wallGablePoints;

	public Roof(int numOfDrawPoints, int numOfEditPoints, double height) {
		super(numOfDrawPoints, numOfEditPoints, height);
	}

	protected void init() {
		super.init();
		abspoints = points; // there is no need for abspoints. this is hack for foundation bounds.
		wallUpperPoints = new ArrayList<PolygonPoint>();
		wallNormals = new ArrayList<ReadOnlyVector3>();
		walls = new ArrayList<Wall>();

		flattenedMeshesRoot = new Node("Roof Meshes Root");
		root.attachChild(flattenedMeshesRoot);

		mesh = new Mesh("Roof");
		bottomMesh = new Mesh("Roof (bottom)");
		final CullState cullState = new CullState();
		cullState.setCullFace(com.ardor3d.renderer.state.CullState.Face.Front);
		bottomMesh.setRenderState(cullState);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		bottomMesh.setRenderState(ms);

		wireframeMesh = new Line("Roof (wireframe)");
		wireframeMesh.getMeshData().setIndexMode(IndexMode.Lines);
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		wireframeMesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wireframeMesh.getSceneHints().setCastsShadows(false);
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		root.attachChild(wireframeMesh);

		updateTextureAndColor(Scene.getInstance().isTextureEnabled());

		getEditPointShape(0).setDefaultColor(ColorRGBA.CYAN);
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
			final boolean bottomMeshVisible = height > 0;
			bottomMesh.getSceneHints().setCullHint(bottomMeshVisible ? CullHint.Inherit : CullHint.Always);
			bottomMesh.getSceneHints().setPickingHint(PickingHint.Pickable, bottomMeshVisible);

			exploreWallNeighbors((Wall) container);
			processRoofPoints(wallUpperPoints, wallNormals);
			computeGableEditPoints();
			updateEditShapes();

//			fillMeshWithPolygon(bottomMesh, new Polygon(wallUpperPoints));
//			if (!root.hasChild(bottomMesh))
//				root.attachChild(bottomMesh);
			final Polygon polygon = makePolygon(wallUpperPoints);
			addGablePointsToPolygon(polygon);
			fillMeshWithPolygon(mesh, polygon);

			// create roof parts
			int meshIndex = 0;
			MeshLib.groupByPlanner(mesh, flattenedMeshesRoot);
			hideGableMeshes();
			final FloatBuffer wireframeVertexBuffer = wireframeMesh.getMeshData().getVertexBuffer();
			wireframeVertexBuffer.rewind();
			wireframeVertexBuffer.limit(wireframeVertexBuffer.capacity());
			for (final Spatial child : flattenedMeshesRoot.getChildren()) {
				if (child.getSceneHints().getCullHint() != CullHint.Always) {
					child.setUserData(new UserData(this, meshIndex++, false));
					final Mesh mesh = (Mesh) child;
					MeshLib.addConvexWireframe(wireframeVertexBuffer, mesh.getMeshData().getVertexBuffer());
					if (!Scene.getInstance().isTextureEnabled())
						mesh.setDefaultColor(defaultColor);
					final MaterialState ms = new MaterialState();
					ms.setColorMaterial(ColorMaterial.Diffuse);
					mesh.setRenderState(ms);
				}
			}
			wireframeVertexBuffer.limit(wireframeVertexBuffer.position());
			wireframeMesh.getMeshData().updateVertexCount();
			wireframeMesh.updateModelBound();

//			drawGableWalls();

			for (int i = 0; i < points.size(); i++) {
				Vector3 p = points.get(i);
				getEditPointShape(i).setTranslation(p);
			}

			if (bottomMesh != null)
				bottomMesh.updateModelBound();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addGablePointsToPolygon(final Polygon polygon) {
		// if (gablePoints != null)
		// for (final Vector3 p : gablePoints)
		// polygon.addSteinerPoint(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
	}

	protected void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.out.println("Triangulate exception received with the following polygon:");
			for (TriangulationPoint p : polygon.getPoints())
				System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
			throw e;
		}
		for (TriangulationPoint p : polygon.getPoints())
			System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
		
		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);

		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 2, new TPoint(0, 0, 0), new TPoint(1, 0, 0), new TPoint(0, 1, 0));
		mesh.getMeshData().updateVertexCount();
		mesh.updateModelBound();
	}

	private void exploreWallNeighbors(final Wall startWall) {
		walls.clear();
		wallUpperPoints.clear();
		wallNormals.clear();
		center.set(0, 0, 0);
		startWall.visitNeighbors(new WallVisitor() {
			public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
				walls.add(currentWall);
				final int pointIndex2;
				if (nextSnap != null)
					pointIndex2 = nextSnap.getSnapPointIndexOf(currentWall) + 1;
				else
					pointIndex2 = 0 + 1;
				final int pointIndex1 = pointIndex2 == 1 ? 3 : 1;
				final Vector3 p1 = currentWall.getAbsPoints().get(pointIndex1);
				final Vector3 p2 = currentWall.getAbsPoints().get(pointIndex2);
				final ReadOnlyVector3 normal = currentWall.getFaceDirection();
				addPointToPolygon(p1, normal);
				addPointToPolygon(p2, normal);
			}
		});

		center.multiplyLocal(1.0 / wallUpperPoints.size());
		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
	}

	protected void addPointToPolygon(final Vector3 p, final ReadOnlyVector3 normal) {
		final PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		final int index = wallUpperPoints.indexOf(polygonPoint);
		if (index == -1) {
			wallUpperPoints.add(polygonPoint);
			center.addLocal(p);
			wallNormals.add(normal);
		} else {
			// calculate wall normal in such a way to help in drawing overhang of roofs
			final ReadOnlyVector3 n1 = wallNormals.get(index);
			final double d = 1.0 / MathUtils.cos(n1.normalize(null).smallestAngleBetween(normal) / 2.0); // assuming thickness is 1
			final Vector3 result = n1.add(normal, null).normalizeLocal().multiplyLocal(d);
			wallNormals.set(index, result);
		}
	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		return new Polygon(wallUpperPoints);
	}

	public void flatten(double flattenTime) {
		for (Spatial child : getFlattenedMeshesRoot().getChildren())
			flattenQuadTriangle((Mesh) child, flattenTime);
		mesh.updateModelBound();
		if (bottomMesh != null) {
			bottomMesh.getSceneHints().setCullHint(CullHint.Always);
			bottomMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		}
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

	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
		final Vector3 op = new Vector3();
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			final PolygonPoint p = wallUpperPoints.get(i);
			op.set(wallNormals.get(i)).multiplyLocal(OVERHANG_LENGHT);
			op.addLocal(p.getX(), p.getY(), p.getZ());
			p.set(op.getX(), op.getY(), op.getZ());
		}
		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
	}

	public void setGable(int index) {
		if (gableBases == null)
			gableBases = new ArrayList<Vector3[]>();
		final Vector3[] base = findBasePoints((Mesh) getFlattenedMeshesRoot().getChild(index), null);
		gableBases.add(base);
		draw();
	}

	private void computeGableEditPoints() {
		if (gableBases == null)
			return;
		final ArrayList<Vector3> meshUpperPoints = new ArrayList<Vector3>();
		for (final Vector3[] base : gableBases) {
			for (final Spatial mesh : getFlattenedMeshesRoot().getChildren()) {
				meshUpperPoints.clear();
				final Vector3[] meshBase = findBasePoints((Mesh) mesh, meshUpperPoints);
				if (meshBase[0].equals(base[0]) && meshBase[1].equals(base[1])) {
					final Vector3 n = meshBase[1].subtract(meshBase[0], null).crossLocal(Vector3.UNIT_Z).normalizeLocal();

					if (gablePoints == null)
						gablePoints = new ArrayList<Vector3>();
					else
						gablePoints.clear();
//					if (wallGablePoints == null)
					final ArrayList<Vector3> wallGablePoints = new ArrayList<Vector3>();
					for (final Vector3 editPoint : points) {
						for (final Vector3 meshPoint : meshUpperPoints) {
							if (meshPoint.distance(editPoint) < MathUtils.ZERO_TOLERANCE) {
								double distance = -editPoint.subtract(meshBase[0], null).dot(n);
								distance += -Math.signum(distance)*0.0001;	// in order to avoid crazy roof that stretches to floor
								editPoint.addLocal(n.multiply(distance, null));
								wallGablePoints.add(editPoint.clone());
							}
						}
					}
					drawGableWall(base, wallGablePoints);
					break;
				}
			}
		}
	}

	public Vector3[] findBasePoints(final Mesh mesh, final ArrayList<Vector3> storeUpperPoints) {
		final Vector3[] base = new Vector3[2];
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		while (vertexBuffer.hasRemaining()) {
			final Vector3 meshPoint = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			if (meshPoint.getZ() - container.getAbsPoints().get(1).getZ() < MathUtils.ZERO_TOLERANCE) {
				if (base[0] == null)
					base[0] = meshPoint;
				else if (!meshPoint.equals(base[0])) {
					base[1] = meshPoint;
				}
			} else if (storeUpperPoints != null)
				storeUpperPoints.add(meshPoint);
		}
		return base;
	}

	private void hideGableMeshes() {
		if (gableBases == null)
			return;
		// for (final int index : gableMeshIndices)
		// getFlattenedMeshesRoot().getChild(index).getSceneHints().setCullHint(CullHint.Always);

		for (final Vector3[] base_i : gableBases) {
			for (final Spatial mesh : getFlattenedMeshesRoot().getChildren()) {
				final Vector3[] base = findBasePoints((Mesh) mesh, null);
				if (isSameBasePoints(base_i, base)) {
					mesh.getSceneHints().setCullHint(CullHint.Always);
				}
			}
		}

	}

	public boolean isSameBasePoints(final Vector3[] base_1, final Vector3[] base_2) {
		// return (base_2[0].equals(base_1[0]) && base_2[1].equals(base_1[1])) || (base_2[0].equals(base_1[1]) && base_2[1].equals(base_1[0]));
		return (base_2[0].distance(base_1[0]) < MathUtils.ZERO_TOLERANCE && base_2[1].distance(base_1[1]) < MathUtils.ZERO_TOLERANCE) || (base_2[0].distance(base_1[1]) < MathUtils.ZERO_TOLERANCE && base_2[1].distance(base_1[0]) < MathUtils.ZERO_TOLERANCE);
	}

	private void drawGableWall(final Vector3[] targetBase, final ArrayList<Vector3> wallGablePoints) {
		final Vector3[] wallBase = new Vector3[2];
		wallBase[0] = new Vector3();
		wallBase[1] = new Vector3();
		int normalIndex = 0;
		for (final Wall wall : walls) {
			final Vector3 wallFirstPoint = wall.getAbsPoints().get(1);
			wallBase[0].set(wallNormals.get(normalIndex)).multiplyLocal(OVERHANG_LENGHT).addLocal(wallFirstPoint);
			wallBase[1].set(wallNormals.get(normalIndex + 1 < wallNormals.size() ? normalIndex + 1 : 0)).multiplyLocal(OVERHANG_LENGHT).addLocal(wall.getAbsPoints().get(3));
			normalIndex++;
			final Vector3 d = wall.getFaceDirection().multiply(OVERHANG_LENGHT, null).negateLocal();
			if (isSameBasePoints(targetBase, wallBase)) {
				for (final Vector3 p : wallGablePoints)
					p.addLocal(d);
				Collections.sort(wallGablePoints, new Comparator<Vector3>() {

					@Override
					public int compare(Vector3 o1, Vector3 o2) {
						if (o1.distance(wallFirstPoint) > o2.distance(wallFirstPoint))
							return -1;
						else
							return 1;						
					}
					
				});
				wall.setGablePoints(wallGablePoints);
				wall.draw();
				break;
			}
		}
	}
}
