package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public abstract class Roof extends HousePart {
	private static final long serialVersionUID = 1L;
	// protected static final double getGridSize() = 0.5;
	private static final double OVERHANG_LENGHT = 0.2; // 0.01; //0.2;
	protected transient Node roofPartsRoot;
	private transient ArrayList<PolygonPoint> wallUpperPoints;
	private transient ArrayList<ReadOnlyVector3> wallNormals;
	private transient Map<Node, ReadOnlyVector3> orgCenters;
	private transient Line wireframeMesh;
	private transient ArrayList<Wall> walls;
	private transient HousePart previousContainer;
	// private transient Mesh gridsMesh;
	private ArrayList<Wall> gableWalls = null;
	private Map<Integer, ArrayList<Wall>> gableEditPointToWallMap = null;
	private Map<Integer, ArrayList<Integer>> gableRoofPartToEditPointMap = null;
	private transient Map<Spatial, Boolean> roofPartPrintVerticalMap;

	public Roof(int numOfDrawPoints, int numOfEditPoints, double height) {
		super(numOfDrawPoints, numOfEditPoints, height);
	}

	protected void init() {
		super.init();
		relativeToHorizontal = true; // TODO move all parameters of HousePart constructor to init
		orgCenters = new HashMap<Node, ReadOnlyVector3>();
		wallUpperPoints = new ArrayList<PolygonPoint>();
		wallNormals = new ArrayList<ReadOnlyVector3>();
		walls = new ArrayList<Wall>();
		roofPartPrintVerticalMap = new Hashtable<Spatial, Boolean>();

		roofPartsRoot = new Node("Roof Meshes Root");
		root.attachChild(roofPartsRoot);

		mesh = new Mesh("Roof");
		mesh.setModelBound(new BoundingBox());

		wireframeMesh = new Line("Roof (wireframe)");
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setModelBound(new BoundingBox());
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		Util.disablePickShadowLight(wireframeMesh);
		root.attachChild(wireframeMesh);

		// gridsMesh = new Line("Grids");
		// gridsMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(2));
		// gridsMesh.setDefaultColor(ColorRGBA.BLUE);
		// gridsMesh.setModelBound(new BoundingBox());
		// Util.disablePickShadowLight(gridsMesh);
		// root.attachChild(gridsMesh);
		// setGridsVisible(false);

		getEditPointShape(0).setDefaultColor(ColorRGBA.CYAN);
	}

	protected void drawMesh() {
		try {
			/* undo the effect of wall stretch on all walls if roof is moved to new walls */
			if (previousContainer != container) {
				previousContainer = container;
				for (final Wall wall : walls) {
					wall.setRoof(null);
					wall.draw();
				}
			}

			if (container == null) {
				roofPartsRoot.getSceneHints().setCullHint(CullHint.Always);
				wireframeMesh.getSceneHints().setCullHint(CullHint.Always);
				setEditPointsVisible(false);
				return;
			}
			roofPartsRoot.getSceneHints().setCullHint(CullHint.Inherit);
			wireframeMesh.getSceneHints().setCullHint(CullHint.Inherit);

			exploreWallNeighbors((Wall) container);
			processRoofPoints(wallUpperPoints, wallNormals);
			computeGableEditPoints();
			final Polygon polygon = makePolygon(wallUpperPoints);
			fillMeshWithPolygon(mesh, polygon);
			// create roof parts
			MeshLib.groupByPlanner(mesh, roofPartsRoot);
			setAnnotationsVisible(drawAnnotations);
			hideGableRoofParts();
			int roofPartIndex = 0;
			for (final Spatial child : roofPartsRoot.getChildren()) {
				if (child.getSceneHints().getCullHint() != CullHint.Always) {
					final Mesh mesh = (Mesh) ((Node) child).getChild(0);
					mesh.setUserData(new UserData(this, roofPartIndex, false));
					if (!Scene.getInstance().isTextureEnabled())
						mesh.setDefaultColor(defaultColor);
					final MaterialState ms = new MaterialState();
					ms.setColorMaterial(ColorMaterial.Diffuse);
					mesh.setRenderState(ms);
				}
				roofPartIndex++;
			}
			drawWireframe();
			;
			updateTextureAndColor(Scene.getInstance().isTextureEnabled());
			root.updateGeometricState(0);
			// drawGrids(getGridSize());

			// flattenInit();

			// flatten(0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// computeOrientedBoundingBox();
	}

	protected void drawWalls() {
		if (container != null)
			((Wall) container).visitNeighbors(new WallVisitor() {
				public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
					currentWall.draw();
				}
			});
	}

	protected void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			System.out.println("Triangulate exception received with the following polygon:");
			for (TriangulationPoint p : polygon.getPoints())
				System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
			throw e;
		}
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
		startWall.visitNeighbors(new WallVisitor() {
			public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
				if (currentWall.isDrawCompleted()) {
					walls.add(currentWall);
					currentWall.setRoof(Roof.this);
					final int pointIndex2;
					if (nextSnap != null)
						pointIndex2 = nextSnap.getSnapPointIndexOf(currentWall) + 1;
					else
						pointIndex2 = 0 + 1;
					final int pointIndex1 = pointIndex2 == 1 ? 3 : 1;
					final Vector3 p1 = currentWall.getAbsPoint(pointIndex1);
					final Vector3 p2 = currentWall.getAbsPoint(pointIndex2);
					final ReadOnlyVector3 normal = currentWall.getFaceDirection();
					addPointToPolygon(p1, normal);
					addPointToPolygon(p2, normal);
				}
			}
		});
	}

	protected void addPointToPolygon(final Vector3 p, final ReadOnlyVector3 normal) {
		final PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		final int index = wallUpperPoints.indexOf(polygonPoint);
		if (index == -1) {
			wallUpperPoints.add(polygonPoint);
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

	@Override
	public void computeOrientedBoundingBox() {
		orgCenters.clear();
//		if (flatten)
//			flatten(1.0);

		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
				final Mesh roofPartMesh = (Mesh) ((Node) roofPartNode).getChild(0);
				computeOrientedBoundingBox(roofPartMesh);
				orgCenters.put((Node) roofPartNode, new Vector3(roofPartMesh.getWorldBound().getCenter()));
				System.out.print(roofPartMesh.getWorldBound().getCenter());
				System.out.println();
			}
		}
//		if (flatten)
//			flatten(0.0);
	}

	// @Override
	// protected void computeOrientedBoundingBox(final Mesh mesh) {
	// orgCenters.put((Node) mesh.getParent(), null);
	// flattenQuadTriangle((Node) mesh.getParent(), 1.0);
	// super.computeOrientedBoundingBox(mesh);
	// orgCenters.put((Node) mesh.getParent(), new Vector3(mesh.getWorldBound().getCenter()));
	// System.out.print(mesh.getWorldBound().getCenter());;
	// System.out.println();
	// // flattenQuadTriangle((Node) mesh.getParent(), 0.0);
	// }

	public void flattenInit() {
		// if (orgCenters == null)
		// orgCenters = new HashMap<Node, ReadOnlyVector3>();
		// else
		// orgCenters.clear();
		flatten(1.0);
		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			roofPartNode.setTranslation(0, 0, 0);

			final Mesh mesh = (Mesh) ((Node) roofPartNode).getChild(0);
			// computeOrientedBoundingBox(mesh);
			// The following code is needed because the center of bounding box is not accurate. If oriented bounding box is usde then this code is no longer required.
			final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
			buf.rewind();
			final Vector3 p = new Vector3(buf.get(), buf.get(), buf.get());
			roofPartNode.getTransform().applyForward(p);

			final Vector3 orgCenter = new Vector3(roofPartNode.getWorldBound().getCenter());
			orgCenter.setY(p.getY());
			orgCenters.put((Node) roofPartNode, orgCenter);
		}
	}

	public void flatten(final double flattenTime) {
		for (Spatial child : getRoofPartsRoot().getChildren()) {
			if (child.getSceneHints().getCullHint() != CullHint.Always)
				flattenQuadTriangle((Node) child, flattenTime);
		}
		root.updateGeometricState(0);
	}

	private void flattenQuadTriangle(final Node roofPartNode, final double flattenTime) {
		final ReadOnlyVector3 normal = (ReadOnlyVector3) roofPartNode.getUserData();
		final double angleZ = Util.angleBetween(new Vector3(normal.getX(), normal.getY(), 0).normalizeLocal(), Vector3.NEG_UNIT_Y, Vector3.UNIT_Z);
		final Matrix3 m1 = new Matrix3().fromAngles(0, 0, flattenTime * angleZ);

		final ReadOnlyVector3 normal2 = m1.applyPost(normal, null);
		final double angleX = Util.angleBetween(normal2, Vector3.NEG_UNIT_Y, Vector3.UNIT_X);
		final Matrix3 m2 = new Matrix3().fromAngles(flattenTime * angleX, 0, 0);

		final Matrix3 matrix = m2.multiplyLocal(m1);

		final Boolean isVertical = roofPartPrintVerticalMap.get(roofPartNode);
		if (isVertical != null && isVertical)
			new Matrix3().fromAngles(0, -Math.PI / 2.0 * flattenTime, 0).multiply(matrix, matrix);

		roofPartNode.setRotation(matrix);

		ReadOnlyVector3 orgCenter = orgCenters.get(roofPartNode);
		System.out.println(orgCenter);

		if (orgCenter == null)
			orgCenter = Vector3.ZERO;
		final Vector3 targetPrintCenter = ((UserData) ((Node) roofPartNode).getChild(0).getUserData()).getPrintCenter();
		if (!targetPrintCenter.equals(Vector3.ZERO))
			roofPartNode.setTranslation(targetPrintCenter.subtract(orgCenter, null).multiplyLocal(flattenTime));
//		System.out.println(roofPartNode.getTranslation());
	}

	@Override
	protected void hideAnnotations() {
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			for (final Spatial sizeAnnot : ((Node) ((Node) roofPart).getChild(1)).getChildren())
				sizeAnnot.getSceneHints().setCullHint(CullHint.Always);
			for (final Spatial sizeAnnot : ((Node) ((Node) roofPart).getChild(2)).getChildren())
				sizeAnnot.getSceneHints().setCullHint(CullHint.Always);
		}
	}

	@Override
	public void drawAnnotations() {
		if (container == null)
			return;

		for (Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				int annotCounter = 0, angleAnnotCounter = 0;
				final Node roofPartNode = (Node) roofPart;
				final FloatBuffer buf = ((Mesh) roofPartNode.getChild(0)).getMeshData().getVertexBuffer();

				final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeConvexHull(buf);

				final ReadOnlyVector3 normal = (ReadOnlyVector3) roofPart.getUserData();
				final int n = convexHull.size() - 1;
				for (int i = 0; i < n; i++) {
					final ReadOnlyVector3 p1 = convexHull.get(i);
					final ReadOnlyVector3 p2 = convexHull.get(i + 1);
					final ReadOnlyVector3 p3 = convexHull.get((i + 2) % n);

					// Size annotation
					final ReadOnlyVector3 center = p1.add(p2, null).addLocal(p3).multiplyLocal(1.0 / 3.0);
					final SizeAnnotation sizeAnnot = fetchSizeAnnot(annotCounter++, (Node) roofPartNode.getChild(1));
					sizeAnnot.setRange(p2, p3, center, normal, false, Align.Center, true, true, Scene.isDrawAnnotationsInside());
					sizeAnnot.setLineWidth(original == null ? 1f : 2f);
					if (Scene.isDrawAnnotationsInside())
						sizeAnnot.setColor(ColorRGBA.WHITE);
					else
						sizeAnnot.setColor(ColorRGBA.BLACK);

					// Angle annotations
					final AngleAnnotation angleAnnot = fetchAngleAnnot(angleAnnotCounter++, (Node) roofPartNode.getChild(2));
					angleAnnot.setLineWidth(original == null ? 1f : 2f);
					angleAnnot.setRange(p2, p1, p3, normal);
				}
			}
		}
	}

	protected void drawWireframe() {
		if (container == null)
			return;

		final ArrayList<ArrayList<ReadOnlyVector3>> convexHulls = new ArrayList<ArrayList<ReadOnlyVector3>>();
		int totalVertices = 0;

		for (Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Node roofPartNode = (Node) roofPart;

				final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeConvexHull(((Mesh) roofPartNode.getChild(0)).getMeshData().getVertexBuffer());
				convexHulls.add(convexHull);
				totalVertices += convexHull.size();
			}
		}

		final FloatBuffer buf;
		if (wireframeMesh.getMeshData().getVertexBuffer().capacity() >= totalVertices * 2 * 3) {
			buf = wireframeMesh.getMeshData().getVertexBuffer();
			buf.limit(buf.capacity());
			buf.rewind();
		} else {
			buf = BufferUtils.createVector3Buffer(totalVertices * 2);
			wireframeMesh.getMeshData().setVertexBuffer(buf);
		}

		for (final ArrayList<ReadOnlyVector3> convexHull : convexHulls) {
			for (int i = 0; i < convexHull.size(); i++) {
				final ReadOnlyVector3 p1 = convexHull.get(i);
				final ReadOnlyVector3 p2 = convexHull.get((i + 1) % convexHull.size());

				buf.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
				buf.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
			}
		}
		buf.limit(buf.position());
		;
		wireframeMesh.getMeshData().updateVertexCount();
	}

	public int drawLabels(int printSequence) {
		for (Spatial roofPartNode : roofPartsRoot.getChildren()) {
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
				final String text = "(" + (printSequence++ + 1) + ")";
				final BMText label = (BMText) ((Node) roofPartNode).getChild(3);
				label.getSceneHints().setCullHint(CullHint.Inherit);
				label.setText(text);
			}
		}
		return printSequence;
	}

	public void hideLabels() {
		for (Spatial roofPartNode : roofPartsRoot.getChildren())
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always)
				((Node) roofPartNode).getChild(3).getSceneHints().setCullHint(CullHint.Always);
	}

	public void updateTextureAndColor(final boolean textureEnabled) {
		if (roofPartsRoot != null) {
			for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
				if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
					final Mesh mesh = (Mesh) ((Node) roofPartNode).getChild(0);
					if (textureEnabled) {
						final TextureState ts = new TextureState();
						ts.setTexture(TextureManager.load(textureFileName, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
						mesh.setRenderState(ts);
						mesh.setDefaultColor(ColorRGBA.WHITE);

					} else {
						mesh.clearRenderState(StateType.Texture);
						mesh.setDefaultColor(defaultColor);
					}
				}
			}
		}
	}

	public Node getRoofPartsRoot() {
		return roofPartsRoot;
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
	}

	private void convertFromVersion_0_4_1() {
		for (final Wall wall : gableWalls) {
			final Vector3[] base = { wall.getAbsPoint(0), wall.getAbsPoint(2) };
			int roofPartIndex = 0;
			for (final Spatial roofPartNode : getRoofPartsRoot().getChildren()) {
				final ReadOnlyVector3[] meshBase = findBasePoints((Mesh) ((Node) roofPartNode).getChild(0), null);
				if (meshBase != null && isSameBasePoints(base, meshBase)) {
					setGable(roofPartIndex, true);
				}
				roofPartIndex++;
			}
		}
	}

	public void setGable(final int roofPartIndex, final boolean isGable) {
		System.out.println("setGable(" + roofPartIndex + ", " + isGable + ")");
		if (gableWalls == null)
			gableWalls = new ArrayList<Wall>();
		if (gableEditPointToWallMap == null)
			gableEditPointToWallMap = new Hashtable<Integer, ArrayList<Wall>>();
		if (gableRoofPartToEditPointMap == null)
			gableRoofPartToEditPointMap = new Hashtable<Integer, ArrayList<Integer>>();

		final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints = new ArrayList<ReadOnlyVector3>();
		final ReadOnlyVector3[] roofMeshBase = findBasePoints((Mesh) ((Node) getRoofPartsRoot().getChild(roofPartIndex)).getChild(0), roofPartMeshUpperPoints);
		final Wall wall = findGableWall(roofMeshBase);
		if (isGable) {
			final ArrayList<Integer> editPoints = new ArrayList<Integer>();
			for (final ReadOnlyVector3 roofPartMeshUpperPoint : roofPartMeshUpperPoints) {
				double smallestDistanceToEditPoint = Double.MAX_VALUE;
				int nearestEditPointIndex = -1;
				// select the nearest point so that one edit point per upper mesh point is selected
				for (int i = 0; i < points.size(); i++) {
					final Vector3 editPoint = getAbsPoint(i);
					final double distanceToEditPoint = roofPartMeshUpperPoint.distance(editPoint);
					if (distanceToEditPoint < smallestDistanceToEditPoint) {
						smallestDistanceToEditPoint = distanceToEditPoint;
						nearestEditPointIndex = i;
					}
				}
				// if (gableEditPointToWallMap.containsKey(nearestEditPointIndex))
				// gableEditPointToWallMap.get(nearestEditPointIndex).add(wall);
				// else {
				// final ArrayList<Wall> walls = new ArrayList<Wall>(2);
				// walls.add(wall);
				// gableEditPointToWallMap.put(nearestEditPointIndex, walls);
				// }
				if (gableEditPointToWallMap.get(nearestEditPointIndex) == null)
					gableEditPointToWallMap.put(nearestEditPointIndex, new ArrayList<Wall>(2));
				gableEditPointToWallMap.get(nearestEditPointIndex).add(wall);
				editPoints.add(nearestEditPointIndex);
			}
			gableRoofPartToEditPointMap.put(roofPartIndex, editPoints);
			gableWalls.add(wall);
		} else {
			for (final int editPointIndex : gableRoofPartToEditPointMap.get(roofPartIndex))
				gableEditPointToWallMap.remove(editPointIndex);
			gableRoofPartToEditPointMap.remove(roofPartIndex);
			gableWalls.remove(wall);
		}

		draw();
		drawWalls();

	}

	private void computeGableEditPoints() {
		if (gableWalls == null)
			return;
		else if (gableEditPointToWallMap == null) {
			convertFromVersion_0_4_1();
			return;
		}
		for (final int nearestIndex : gableEditPointToWallMap.keySet()) {
			final Vector3 nearestEditPoint = getAbsPoint(nearestIndex);
			for (final Wall wall : gableEditPointToWallMap.get(nearestIndex)) {
				final ReadOnlyVector3 n = wall.getFaceDirection();
				double distance = -nearestEditPoint.subtract(wall.getAbsPoint(0).addLocal(n.multiply(OVERHANG_LENGHT, null)), null).dot(n);
				distance -= 0.0001; // in order to avoid empty roof part caused by being slightly out of range of roof, and crazy roof that stretches to floor
				nearestEditPoint.addLocal(n.multiply(distance, null));
			}
			points.get(nearestIndex).set(toRelative(nearestEditPoint, container.getContainer()));
		}
	}

	public ReadOnlyVector3[] findBasePoints(final Mesh mesh, final ArrayList<ReadOnlyVector3> storeUpperPoints) {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();

		final ReadOnlyVector3[] base = { new Vector3(buf.get(), buf.get(), buf.get()), new Vector3(buf.get(), buf.get(), buf.get()) };
		if (storeUpperPoints != null) {
			storeUpperPoints.add(base[0]);
			storeUpperPoints.add(base[1]);
		}

		double highestZ = Math.max(base[0].getZ(), base[1].getZ());
		while (buf.hasRemaining()) {
			ReadOnlyVector3 meshPoint = new Vector3(buf.get(), buf.get(), buf.get());
			if (meshPoint.equals(base[0]) || meshPoint.equals(base[1]))
				continue;

			if (meshPoint.getZ() > highestZ)
				highestZ = meshPoint.getZ();

			if (storeUpperPoints != null)
				storeUpperPoints.add(meshPoint);

			if (base[0].getZ() > meshPoint.getZ()) {
				final ReadOnlyVector3 tmp = base[0];
				base[0] = meshPoint;
				meshPoint = tmp;
			}
			if (base[1].getZ() > meshPoint.getZ()) {
				base[1] = meshPoint;
			}
		}

		if (highestZ <= base[0].getZ() && highestZ <= base[0].getZ())
			return null;

		if (storeUpperPoints != null) {
			storeUpperPoints.remove(base[0]);
			storeUpperPoints.remove(base[1]);
		}
		return base;
	}

	private void hideGableRoofParts() {
		if (gableWalls == null)
			return;
		// for (final Spatial roofPart : getRoofPartsRoot().getChildren()) {
		// roofPart.getSceneHints().setCullHint(CullHint.Inherit);
		// roofPart.getSceneHints().setPickingHint(PickingHint.Pickable, true);
		// }

		for (final Wall wall : gableWalls) {
			if (wall == null)
				continue;
			final Vector3[] base_i = { wall.getAbsPoint(0), wall.getAbsPoint(2) };
			for (final Spatial roofPart : getRoofPartsRoot().getChildren()) {
				final ReadOnlyVector3[] base = findBasePoints((Mesh) ((Node) roofPart).getChild(0), null);
				if (base != null && isSameBasePoints(base_i, base)) {
					roofPart.getSceneHints().setCullHint(CullHint.Always);
					roofPart.getSceneHints().setPickingHint(PickingHint.Pickable, false);
					break;
				}
			}
		}
	}

	public boolean isSameBasePoints(final ReadOnlyVector3[] base_1, final ReadOnlyVector3[] base_2) {
		final double maxOverhangDistance = MathUtils.sqrt(2 * OVERHANG_LENGHT * OVERHANG_LENGHT) * 2;
		final Vector2 p1a = new Vector2(base_1[0].getX(), base_1[0].getY());
		final Vector2 p1b = new Vector2(base_1[1].getX(), base_1[1].getY());
		final Vector2 p2a = new Vector2(base_2[0].getX(), base_2[0].getY());
		final Vector2 p2b = new Vector2(base_2[1].getX(), base_2[1].getY());
		return (p1a.distance(p2a) <= maxOverhangDistance && p1b.distance(p2b) <= maxOverhangDistance) || (p1a.distance(p2b) <= maxOverhangDistance && p1b.distance(p2a) <= maxOverhangDistance);
	}

	private Wall findGableWall(final ReadOnlyVector3[] targetBase) {
		for (final Wall wall : walls) {
			if (isSameBasePoints(targetBase, new Vector3[] { wall.getAbsPoint(0), wall.getAbsPoint(2) }))
				return wall;
		}
		return null;
	}

	public ArrayList<Spatial> findMeshesContainingEditPoints(final ArrayList<Vector3> editpoints) {
		final ArrayList<Spatial> meshes = new ArrayList<Spatial>();
		for (final Spatial mesh : getRoofPartsRoot().getChildren()) {
			boolean foundAll = true;
			for (final Vector3 p : editpoints) {
				final FloatBuffer buf = ((Mesh) mesh).getMeshData().getVertexBuffer();
				buf.rewind();
				boolean found = false;
				while (buf.hasRemaining() && !found) {
					if (p.distance(new Vector3(buf.get(), buf.get(), buf.get())) < MathUtils.ZERO_TOLERANCE)
						found = true;
				}
				if (!found) {
					foundAll = false;
					break;
				}
			}
			if (foundAll)
				meshes.add(mesh);
		}
		return meshes;
	}

	@Override
	public void delete() {
		super.delete();
		for (final Wall wall : walls) {
			wall.setRoof(null);
			wall.draw();
		}
	}

	@Override
	public void setOriginal(final HousePart original) {
		this.original = original;
		this.root.detachChild(pointsRoot);
		this.root.detachChild(wireframeMesh);
		this.root.detachChild(roofPartsRoot);
		this.roofPartsRoot = ((Roof) original).roofPartsRoot.makeCopy(false);
		for (int i = 0; i < roofPartsRoot.getNumberOfChildren(); i++) {
			if (roofPartsRoot.getChild(i).getSceneHints().getCullHint() != CullHint.Always) {
				// ((Node) roofPartsRoot.getChild(i)).getChild(1).getSceneHints().setCullHint(CullHint.Always);
				final UserData orgUserData = (UserData) ((Node) ((Roof) original).roofPartsRoot.getChild(i)).getChild(0).getUserData();
				final Mesh mesh = (Mesh) ((Node) roofPartsRoot.getChild(i)).getChild(0);
				mesh.setUserData(new UserData(this, orgUserData.getIndex(), false));
				roofPartsRoot.getChild(i).setUserData(((Roof) original).roofPartsRoot.getChild(i).getUserData());

				// final BoundingVolume modelBound = ((Mesh) ((Node) ((Roof) original).roofPartsRoot.getChild(i)).getChild(0)).getModelBound();
				// mesh.setModelBound(modelBound);
				// modelBound.transform(new Transform(), modelBound);
			}
		}
		root.attachChild(roofPartsRoot);
		drawAnnotations();
//		computeOrientedBoundingBox(true);
		root.updateWorldBound(true);
	}

	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index), container == null ? null : container.getContainer());
	}

	@Override
	protected ReadOnlyVector3 getCenter() {
		final Vector3 min = new Vector3(wallUpperPoints.get(0).getX(), wallUpperPoints.get(0).getY(), wallUpperPoints.get(0).getZ());
		final Vector3 max = new Vector3(wallUpperPoints.get(0).getX(), wallUpperPoints.get(0).getY(), wallUpperPoints.get(0).getZ());
		for (final PolygonPoint p : wallUpperPoints) {
			min.setX(Math.min(min.getX(), p.getX()));
			min.setY(Math.min(min.getY(), p.getY()));
			max.setX(Math.max(max.getX(), p.getX()));
			max.setY(Math.max(max.getY(), p.getY()));
		}
		return min.addLocal(max).multiplyLocal(new Vector3(0.5, 0.5, 0)).addLocal(0, 0, container.getPoints().get(1).getZ());
	}

	@Override
	public void setAnnotationsVisible(boolean visible) {
		super.setAnnotationsVisible(visible);
		final CullHint cull = visible ? CullHint.Inherit : CullHint.Always;
		if (roofPartsRoot != null)
			for (final Spatial roofPart : roofPartsRoot.getChildren()) {
				((Node) roofPart).getChild(1).getSceneHints().setCullHint(cull);
				((Node) roofPart).getChild(2).getSceneHints().setCullHint(cull);
			}
	}

	@Override
	public void drawGrids(final double gridSize) {
		// final ReadOnlyVector3 p0 = getAbsPoint(0);
		// final ReadOnlyVector3 p1 = getAbsPoint(1);
		// final ReadOnlyVector3 p2 = getAbsPoint(2);
		final BoundingBox bounds = (BoundingBox) root.getWorldBound();
		final ReadOnlyVector3 width = Vector3.UNIT_X.multiply(bounds.getXExtent() * 2, null);
		final ReadOnlyVector3 height = Vector3.UNIT_Y.multiply(bounds.getYExtent() * 2, null);
		final ArrayList<ReadOnlyVector3> points = new ArrayList<ReadOnlyVector3>();
		final ReadOnlyVector3 pMiddle = getAbsPoint(0);
		// final ReadOnlyVector3 pMiddle = width.add(height, null).multiplyLocal(0.5).addLocal(p0);

		final int cols = (int) (width.length() / gridSize);

		for (int col = 0; col < cols / 2 + 1; col++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = width.normalize(null).multiplyLocal(neg * col * gridSize).addLocal(pMiddle).subtractLocal(height.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(height, null);
				points.add(lineP2);
				if (col == 0)
					break;
			}
		}

		final int rows = (int) (height.length() / gridSize);

		for (int row = 0; row < rows / 2 + 1; row++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = height.normalize(null).multiplyLocal(neg * row * gridSize).addLocal(pMiddle).subtractLocal(width.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(width, null);
				points.add(lineP2);
				if (row == 0)
					break;
			}
		}
		if (points.size() < 2)
			return;
		final FloatBuffer buf = BufferUtils.createVector3Buffer(points.size());
		for (final ReadOnlyVector3 p : points)
			// buf.put(p.getXf()).put(p.getYf()).put((float) this.height + 0.01f);
			buf.put(p.getXf()).put(p.getYf()).put(pMiddle.getZf());

		gridsMesh.getMeshData().setVertexBuffer(buf);
		// gridsMesh.updateModelBound();
		// gridsMesh.updateWorldBound(false);
		// gridsMesh.getSceneHints().setCullHint(CullHint.Inherit);
	}

	public void setPrintVertical(final Spatial roofPartNode, final boolean isVertical) {
//		Boolean currentValue = roofPartPrintVerticalMap.get(roofPartNode);
//		if (currentValue == null)
//			currentValue = true;
//		if (currentValue == isVertical)
//		if (!isVertical)
//			return;
		roofPartPrintVerticalMap.put(roofPartNode, isVertical);
//		flatten(1.0); // to trigger rotate 90 degrees
//		computeOrientedBoundingBox();
//		flatten(1.0);
		 flattenQuadTriangle((Node) roofPartNode, 1.0);
		 roofPartNode.updateGeometricState(0);
		orgCenters.put((Node) roofPartNode, null);
		 final Mesh roofPartMesh = (Mesh) ((Node) roofPartNode).getChild(0);
		 computeOrientedBoundingBox(roofPartMesh);
		 orgCenters.put((Node) roofPartNode, new Vector3(roofPartMesh.getWorldBound().getCenter()));
		// flattenQuadTriangle((Node) roofPartNode, 0.0);
	}
}
