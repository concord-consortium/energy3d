package org.concord.energy3d.model;

import java.awt.geom.Path2D;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.undo.UndoManager;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.undo.MakeGableCommand;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.PolygonWithHoles;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume.Type;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public abstract class Roof extends HousePart implements Thermal {

	private static final long serialVersionUID = 1L;
	public static final int TEXTURE_01 = 1;
	public static final int TEXTURE_02 = 2;
	public static final int TEXTURE_03 = 3;
	public static final int TEXTURE_04 = 4;
	public static final int TEXTURE_05 = 5;
	public static final int TEXTURE_06 = 6;
	public static final int TEXTURE_07 = 7;

	public static final double OVERHANG_MIN = 0.01;
	public static final int SOLID = 0;
	public static final int TRANSPARENT = 1;
	public static ReadOnlyVector3 nullVector = new Vector3();

	protected transient Node roofPartsRoot;
	protected transient List<ReadOnlyVector3> wallUpperPoints;
	private transient Map<Mesh, List<ReadOnlyVector3>> dashPointsCache;
	private transient Map<Spatial, Boolean> roofPartPrintVerticalMap;
	private transient Map<Node, ReadOnlyVector3> orgCenters;
	private transient Map<Mesh, Double> areaByPartWithoutOverhang;
	private transient Map<Mesh, Double> areaByPartWithOverhang;
	private transient List<ReadOnlyVector3> wallNormals;
	private transient List<Wall> walls;
	private transient List<ReadOnlyVector3> wallUpperPointsWithoutOverhang;
	private transient HousePart previousContainer;
	protected Map<Integer, List<Wall>> gableEditPointToWallMap = null;
	private double overhangLength = 2.0;
	private double volumetricHeatCapacity = 0.5; // unit: kWh/m^3/C (1 kWh = 3.6 MJ)
	private double uValue = 0.15; // default is R38 (IECC for Massachusetts: https://energycode.pnl.gov/EnergyCodeReqs/index.jsp?state=Massachusetts)
	private int type = SOLID;
	private transient Path2D.Double underlyingWallPath;
	private transient List<Vector2> underlyingWallVerticesOnFoundation;

	final static byte REAL_MESH_INDEX = 6;

	protected class EditState {
		final boolean fitTestRequired;
		final ArrayList<Vector3> orgPoints;
		final double orgHeight;

		public EditState() {
			if (editPointIndex == -1) {
				orgPoints = null;
			} else {
				orgPoints = new ArrayList<Vector3>(points.size());
				for (final Vector3 v : points) {
					orgPoints.add(v.clone());
				}
			}
			if (editPointIndex == -1 || !windowsFit()) {
				fitTestRequired = false;
			} else {
				fitTestRequired = true;
			}
			orgHeight = height;
		}
	}

	public Roof(final int numOfEditPoints) {
		super(1, numOfEditPoints, 5.0);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(uValue)) {
			uValue = 0.15;
		}
		if (Util.isZero(overhangLength)) {
			overhangLength = 2;
		}
		if (Util.isZero(volumetricHeatCapacity)) {
			volumetricHeatCapacity = 0.5;
		}

		orgCenters = new HashMap<Node, ReadOnlyVector3>();
		wallNormals = new ArrayList<ReadOnlyVector3>();
		walls = new ArrayList<Wall>();
		roofPartPrintVerticalMap = new HashMap<Spatial, Boolean>();
		dashPointsCache = new HashMap<Mesh, List<ReadOnlyVector3>>();

		roofPartsRoot = new Node("Roof Meshes Root");
		root.attachChild(roofPartsRoot);

		mesh = new Mesh("Roof");
		mesh.setModelBound(null);

		getEditPointShape(0).setDefaultColor(ColorRGBA.CYAN);

		// cleanup
		if (gableEditPointToWallMap != null) {
			for (final List<Wall> wallList : gableEditPointToWallMap.values()) {
				final Iterator<Wall> walls = wallList.iterator();
				while (walls.hasNext()) {
					if (Scene.getInstance().getParts().indexOf(walls.next()) == -1) {
						walls.remove();
					}
				}
			}
		}
	}

	/** Do not call: For UNDO only */
	public Map<Integer, List<Wall>> getGableEditPointToWallMap() {
		return gableEditPointToWallMap;
	}

	/** Do not call: For UNDO only */
	public void setGableEditPointToWallMap(final Map<Integer, List<Wall>> m) {
		gableEditPointToWallMap = new HashMap<Integer, List<Wall>>();
		for (final Map.Entry<Integer, List<Wall>> entry : m.entrySet()) {
			gableEditPointToWallMap.put(entry.getKey(), new ArrayList<Wall>(entry.getValue()));
		}
	}

	@Override
	public double getGridSize() {
		return SceneManager.getInstance().isFineGrid() ? 0.5 : 2.5;
	}

	@Override
	protected void drawMesh() {
		// undo the effect of wall stretch on all walls if roof is moved to new walls
		if (previousContainer != container) {
			previousContainer = container;
			for (final Wall wall : walls) {
				wall.setRoof(null);
				wall.draw();
			}
		}

		dashPointsCache.clear();
		if (wallUpperPoints == null) {
			wallUpperPoints = new ArrayList<ReadOnlyVector3>();
		} else {
			wallUpperPoints.clear();
		}
		if (container != null) {
			initWallUpperPoints((Wall) container, walls, wallUpperPoints, wallNormals);
		}
		if (!isDrawable()) {
			roofPartsRoot.getSceneHints().setCullHint(CullHint.Always);
			setEditPointsVisible(false);
			return;
		}
		roofPartsRoot.getSceneHints().setCullHint(CullHint.Inherit);

		final ArrayList<Vector3> orgPoints = new ArrayList<Vector3>(points.size());
		for (final ReadOnlyVector3 p : points) {
			orgPoints.add(p.clone());
		}
		wallUpperPointsWithoutOverhang = new ArrayList<ReadOnlyVector3>(wallUpperPoints);
		drawRoof();
		switch (type) {
		case TRANSPARENT:
			for (final Spatial child : roofPartsRoot.getChildren()) {
				((Mesh) ((Node) child).getChild(0)).getSceneHints().setCullHint(CullHint.Always);
				((Mesh) ((Node) child).getChild(REAL_MESH_INDEX)).getSceneHints().setCullHint(CullHint.Always);
			}
			break;
		default:
			for (final Spatial child : roofPartsRoot.getChildren()) {
				((Mesh) ((Node) child).getChild(0)).getSceneHints().setCullHint(CullHint.Inherit);
				((Mesh) ((Node) child).getChild(REAL_MESH_INDEX)).getSceneHints().setCullHint(CullHint.Inherit);
			}
		}
		roofPartsRoot.updateWorldBound(true);
		drawOutline();
		if (Scene.getInstance().areDashedLinesOnRoofShown()) {
			drawDashLines();
		} else {
			for (final Spatial roofPart : roofPartsRoot.getChildren()) {
				if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
					final Mesh dashLinesMesh = (Mesh) ((Node) roofPart).getChild(5);
					dashLinesMesh.setVisible(false);
				}
			}
		}
	}

	private void drawRoof() {
		applyOverhang(wallUpperPoints, wallNormals);
		processRoofEditPoints(wallUpperPoints);
		computeGableEditPoints();
		ensureEditPointsInside();
		final PolygonWithHoles polygon = makePolygon(wallUpperPoints);
		applySteinerPoint(polygon);
		MeshLib.fillMeshWithPolygon(mesh, polygon, null, true, null, null, null, false);
		MeshLib.groupByPlanar(mesh, roofPartsRoot);
		hideGableRoofParts();
		int roofPartIndex = 0;
		for (final Spatial child : roofPartsRoot.getChildren()) {
			((Mesh) ((Node) child).getChild(0)).setUserData(new UserData(this, roofPartIndex, false));
			((Mesh) ((Node) child).getChild(REAL_MESH_INDEX)).setUserData(new UserData(this, roofPartIndex, false));
			roofPartIndex++;
		}
		final List<Window> windows = new ArrayList<Window>();
		for (final HousePart part : children) {
			if (part instanceof Window && part.isDrawable()) {
				windows.add((Window) part);
			}
		}
		MeshLib.applyHoles(roofPartsRoot, windows);
		setAnnotationsVisible(Scene.getInstance().areAnnotationsVisible());
	}

	protected void drawWalls() {
		if (container != null) {
			((Wall) container).visitNeighbors(new WallVisitor() {
				@Override
				public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
					currentWall.draw();
				}
			});
		}
	}

	protected boolean windowsFit() {
		for (final Wall wall : walls) {
			if (!wall.windowsFit()) {
				return false;
			}
		}
		return true;
	}

	private void drawDashLines() {
		if (container == null) {
			return;
		}

		if (lockEdit) {
			for (final Spatial roofPart : roofPartsRoot.getChildren()) {
				if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
					final Node roofPartNode = (Node) roofPart;
					final Mesh dashLinesMesh = (Mesh) roofPartNode.getChild(5);
					dashLinesMesh.setVisible(false);
				}
			}
		} else {
			for (final Spatial roofPart : roofPartsRoot.getChildren()) {
				if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
					final Node roofPartNode = (Node) roofPart;
					final Mesh roofPartMesh = (Mesh) roofPartNode.getChild(0);
					final Mesh dashLinesMesh = (Mesh) roofPartNode.getChild(5);
					final List<ReadOnlyVector3> result = computeDashPoints(roofPartMesh);
					if (result.isEmpty()) {
						dashLinesMesh.setVisible(false);
					} else {
						dashLinesMesh.setVisible(true);
						FloatBuffer vertexBuffer = dashLinesMesh.getMeshData().getVertexBuffer();
						if (vertexBuffer == null || vertexBuffer.capacity() < result.size() * 3) {
							vertexBuffer = BufferUtils.createVector3Buffer(result.size());
							dashLinesMesh.getMeshData().setVertexBuffer(vertexBuffer);
						}
						vertexBuffer.limit(result.size() * 3);
						vertexBuffer.rewind();

						for (final ReadOnlyVector3 p : result) {
							vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
						}

						dashLinesMesh.getMeshData().updateVertexCount();
						dashLinesMesh.updateModelBound();
					}
				}
			}
		}
		updateDashLinesColor();
	}

	public List<ReadOnlyVector3> computeDashPoints(final Mesh roofPartMesh) {
		if (dashPointsCache.get(roofPartMesh) == null) {
			final ArrayList<ReadOnlyVector3> resultBeforeBreak = new ArrayList<ReadOnlyVector3>();
			final ArrayList<ReadOnlyVector3> resultAfterBreak = new ArrayList<ReadOnlyVector3>();
			final boolean[] foundBreak = { false };
			((Wall) container).visitNeighbors(new WallVisitor() {
				@Override
				public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
					final int indexP1, indexP2;
					if (nextSnap != null) {
						indexP2 = nextSnap.getSnapPointIndexOf(currentWall);
						indexP1 = indexP2 == 2 ? 0 : 2;
					} else if (prevSnap != null) {
						indexP1 = prevSnap.getSnapPointIndexOf(currentWall);
						indexP2 = indexP1 == 2 ? 0 : 2;
					} else {
						indexP1 = 0;
						indexP2 = 2;
					}
					final ArrayList<ReadOnlyVector3> array = foundBreak[0] ? resultAfterBreak : resultBeforeBreak;
					final int orgSize = array.size();
					stretchToRoof(array, roofPartMesh, currentWall.getAbsPoint(indexP1), currentWall.getAbsPoint(indexP2), currentWall.points.get(1).getZ());
					if (!foundBreak[0] && array.size() == orgSize) {
						foundBreak[0] = true;
					}
				}
			});
			if (foundBreak[0]) {
				resultAfterBreak.addAll(resultBeforeBreak);
				dashPointsCache.put(roofPartMesh, resultAfterBreak);
			} else {
				dashPointsCache.put(roofPartMesh, resultBeforeBreak);
			}
		}
		return dashPointsCache.get(roofPartMesh);
	}

	public void updateDashLinesColor() {
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Node roofPartNode = (Node) roofPart;
				final Mesh dashLinesMesh = (Mesh) roofPartNode.getChild(5);
				dashLinesMesh.setDefaultColor(ColorRGBA.RED);
			}
		}
	}

	private void stretchToRoof(final ArrayList<ReadOnlyVector3> result, final Mesh roof, final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final double z) {
		final Vector3 dir = p2.subtract(p1, null).multiplyLocal(1, 1, 0);
		final double length = dir.length();
		dir.normalizeLocal();

		Vector3 direction = null;
		ReadOnlyVector3 previousStretchPoint = null;
		boolean firstInsert = false;

		final boolean isSingleMesh = isSingleFlatMesh();
		final double minDistance = STRETCH_ROOF_STEP * 3;
		final Vector3 p = new Vector3();
		for (double d = STRETCH_ROOF_STEP; d < length; d += STRETCH_ROOF_STEP) {
			dir.multiply(d, p).addLocal(p1);
			final ReadOnlyVector3 currentStretchPoint;
			if (isSingleMesh) {
				p.setZ(z);
				currentStretchPoint = p.clone();
			} else {
				currentStretchPoint = findRoofIntersection(roof, p);
			}

			if (currentStretchPoint != null && !firstInsert) {
				result.add(currentStretchPoint);
				firstInsert = true;
			} else if (currentStretchPoint == null) {
				if (previousStretchPoint != null) {
					result.add(previousStretchPoint);
				}
				direction = null;
				firstInsert = false;
			} else {
				final Vector3 currentDirection = currentStretchPoint.subtract(previousStretchPoint, null).normalizeLocal();
				if (direction == null) {
					direction = currentDirection;
				} else if (direction.dot(currentDirection) < 1.0 - MathUtils.ZERO_TOLERANCE) {
					result.add(currentStretchPoint);
					result.add(currentStretchPoint);
					direction = null;
				}
			}
			previousStretchPoint = currentStretchPoint;
		}

		if (previousStretchPoint != null) {
			if (previousStretchPoint.distance(result.get(result.size() - 1)) > minDistance) {
				result.add(previousStretchPoint);
			} else {
				result.remove(result.size() - 1);
			}
		}
	}

	private ReadOnlyVector3 findRoofIntersection(final Mesh roofPart, final ReadOnlyVector3 p) {
		final PickResults pickResults = new PrimitivePickResults();
		PickingUtil.findPick(roofPart, new Ray3(p, Vector3.UNIT_Z), pickResults, false);
		if (pickResults.getNumber() > 0) {
			return pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0);
		} else {
			return null;
		}
	}

	protected void fillMeshWithPolygon(final Mesh mesh, final Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			System.out.println("Triangulate exception received with the following polygon:");
			for (final TriangulationPoint p : polygon.getPoints()) {
				System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
			}
			throw e;
		}
		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 200, new TPoint(0, 0, 0), new TPoint(10, 0, 0), new TPoint(0, 10, 0));
		mesh.getMeshData().updateVertexCount();
		mesh.updateModelBound();
	}

	private void initWallUpperPoints(final Wall startWall, final List<Wall> walls, final List<ReadOnlyVector3> wallUpperPoints, final List<ReadOnlyVector3> wallNormals) {
		walls.clear();
		wallUpperPoints.clear();
		wallNormals.clear();
		startWall.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
				if (currentWall.isFirstPointInserted()) {
					walls.add(currentWall);
					currentWall.setRoof(Roof.this);
					final int pointIndex2 = nextSnap != null ? nextSnap.getSnapPointIndexOf(currentWall) + 1 : 1;
					final int pointIndex1 = pointIndex2 == 1 ? 3 : 1;
					final Vector3 p1 = currentWall.getAbsPoint(pointIndex1);
					final Vector3 p2 = currentWall.getAbsPoint(pointIndex2);
					final ReadOnlyVector3 normal = currentWall.getNormal();
					addPointToPolygon(p1, normal, wallUpperPoints, wallNormals);
					addPointToPolygon(p2, normal, wallUpperPoints, wallNormals);
				}
			}
		});
	}

	protected void addPointToPolygon(final Vector3 p, final ReadOnlyVector3 normal, final List<ReadOnlyVector3> wallUpperPoints, final List<ReadOnlyVector3> wallNormals) {
		int index = -1;
		/* check to see if there is another point with same x,y coords */
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			final ReadOnlyVector3 p_i = wallUpperPoints.get(i);
			if (Util.isEqual(p.getX(), p_i.getX()) && Util.isEqual(p.getY(), p_i.getY())) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			wallUpperPoints.add(p);
			wallNormals.add(normal.clone());
		} else {
			// calculate wall normal in such a way to help in drawing overhang of roofs
			final ReadOnlyVector3 currentNormal = wallNormals.get(index);
			final double d = 1.0 / MathUtils.cos(currentNormal.normalize(null).smallestAngleBetween(normal) / 2.0); // assuming thickness is 1
			final Vector3 newNormal = currentNormal.add(normal, null).normalizeLocal().multiplyLocal(d);
			wallNormals.set(index, newNormal);
		}
	}

	private PolygonWithHoles makePolygon(final List<ReadOnlyVector3> wallUpperPoints) {
		final List<PolygonPoint> polygonPoints = new ArrayList<PolygonPoint>(wallUpperPoints.size());
		for (final ReadOnlyVector3 p : wallUpperPoints) {
			polygonPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		}
		return new PolygonWithHoles(polygonPoints);
	}

	protected abstract Polygon applySteinerPoint(final Polygon polygon);

	@Override
	public void computeOrientedBoundingBox() {
		orgCenters.clear();
		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
				final Mesh roofPartMesh = (Mesh) ((Node) roofPartNode).getChild(0);
				computeOrientedBoundingBox(roofPartMesh);
				orgCenters.put((Node) roofPartNode, new Vector3(roofPartMesh.getWorldBound().getCenter()));
			}
		}
	}

	@Override
	public void flatten(final double flattenTime) {
		for (final Spatial roofPart : getRoofPartsRoot().getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				flattenQuadTriangle((Node) roofPart, flattenTime);
			}
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
		if (isVertical != null && isVertical) {
			new Matrix3().fromAngles(0, -Math.PI / 2.0 * flattenTime, 0).multiply(matrix, matrix);
		}

		roofPartNode.setRotation(matrix);

		ReadOnlyVector3 orgCenter = orgCenters.get(roofPartNode);

		if (orgCenter == null) {
			orgCenter = Vector3.ZERO;
		}
		final Vector3 targetPrintCenter = ((UserData) roofPartNode.getChild(0).getUserData()).getPrintCenter();
		if (!targetPrintCenter.equals(Vector3.ZERO)) {
			roofPartNode.setTranslation(targetPrintCenter.subtract(orgCenter, null).multiplyLocal(flattenTime));
		}
	}

	@Override
	protected void clearAnnotations() {
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				for (final Spatial sizeAnnot : ((Node) ((Node) roofPart).getChild(1)).getChildren()) {
					sizeAnnot.getSceneHints().setCullHint(CullHint.Always);
				}
				for (final Spatial sizeAnnot : ((Node) ((Node) roofPart).getChild(2)).getChildren()) {
					sizeAnnot.getSceneHints().setCullHint(CullHint.Always);
				}
			}
		}
	}

	@Override
	public void drawAnnotations() {
		if (container == null) {
			return;
		}
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				int annotCounter = 0, angleAnnotCounter = 0;
				final Node roofPartNode = (Node) roofPart;
				final FloatBuffer buf = ((Mesh) roofPartNode.getChild(0)).getMeshData().getVertexBuffer();

				final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeOutline(buf);

				final ReadOnlyVector3 normal = (ReadOnlyVector3) roofPart.getUserData();
				final int n = convexHull.size();
				for (int i = 0; i < n; i++) {
					final ReadOnlyVector3 p1 = convexHull.get(i);
					final ReadOnlyVector3 p2 = convexHull.get((i + 1) % n);
					final ReadOnlyVector3 p3 = convexHull.get((i + 2) % n);

					// Size annotation
					final ReadOnlyVector3 center = p1.add(p2, null).addLocal(p3).multiplyLocal(1.0 / 3.0);
					final SizeAnnotation sizeAnnot = fetchSizeAnnot(annotCounter++, (Node) roofPartNode.getChild(1));
					final boolean drawAnnotationsInside = Scene.isDrawAnnotationsInside();
					sizeAnnot.setRange(p2, p3, center, normal, false, Align.Center, true, true, drawAnnotationsInside);
					sizeAnnot.setLineWidth(original == null ? 1f : 2f);
					if (drawAnnotationsInside) {
						sizeAnnot.setColor(ColorRGBA.WHITE);
					} else {
						sizeAnnot.setColor(ColorRGBA.BLACK);
					}

					// Angle annotations
					final AngleAnnotation angleAnnot = fetchAngleAnnot(angleAnnotCounter++, (Node) roofPartNode.getChild(2));
					angleAnnot.setLineWidth(original == null ? 1f : 2f);
					angleAnnot.setRange(p2, p1, p3, normal);
				}
			}
		}
	}

	protected void drawOutline() {
		if (container == null) {
			return;
		}
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Node roofPartNode = (Node) roofPart;
				final Mesh outlineMesh = (Mesh) roofPartNode.getChild(4);

				final Mesh mesh = (Mesh) roofPartNode.getChild(0);
				final ArrayList<ReadOnlyVector3> outlinePoints = MeshLib.computeOutline(mesh.getMeshData().getVertexBuffer());
				int totalVertices = outlinePoints.size();
				for (final HousePart part : children) {
					if (part instanceof Window) {
						totalVertices += 8;
					}
				}

				final FloatBuffer buf;
				if (outlineMesh.getMeshData().getVertexBuffer().capacity() >= totalVertices * 2 * 3) {
					buf = outlineMesh.getMeshData().getVertexBuffer();
					buf.limit(buf.capacity());
					buf.rewind();
				} else {
					buf = BufferUtils.createVector3Buffer(totalVertices * 2);
					outlineMesh.getMeshData().setVertexBuffer(buf);
				}

				// draw roof outline
				for (int i = 0; i < outlinePoints.size(); i++) {
					final ReadOnlyVector3 p1 = outlinePoints.get(i);
					final ReadOnlyVector3 p2 = outlinePoints.get((i + 1) % outlinePoints.size());

					buf.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
					buf.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				}

				// draw skylights outline
				final int[] windowIndices = new int[] { 0, 2, 3, 1 };
				for (final HousePart part : children) {
					if (part instanceof Window && part.isDrawable() && ((Window) part).getRoofIndex() == ((UserData) mesh.getUserData()).getEditPointIndex()) {
						for (int i = 0; i < part.getPoints().size(); i++) {
							final ReadOnlyVector3 p1 = part.getAbsPoint(windowIndices[i]);
							final ReadOnlyVector3 p2 = part.getAbsPoint(windowIndices[(i + 1) % part.getPoints().size()]);

							buf.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
							buf.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
						}
					}
				}

				buf.limit(buf.position());
				outlineMesh.getMeshData().updateVertexCount();
				outlineMesh.updateModelBound();
			}
		}
	}

	@Override
	public int drawLabels(int printSequence) {
		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
				final String text = "(" + (printSequence++ + 1) + ")";
				final BMText label = (BMText) ((Node) roofPartNode).getChild(3);
				label.getSceneHints().setCullHint(CullHint.Inherit);
				label.setText(text);
			}
		}
		return printSequence;
	}

	@Override
	public void hideLabels() {
		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
				((Node) roofPartNode).getChild(3).getSceneHints().setCullHint(CullHint.Always);
			}
		}
	}

	@Override
	public void updateTextureAndColor() {
		if (roofPartsRoot != null) {
			for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
				if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
					updateTextureAndColor((Mesh) ((Node) roofPartNode).getChild(REAL_MESH_INDEX), getColor() == null ? Scene.getInstance().getRoofColor() : getColor());
				}
			}
		}
	}

	public Node getRoofPartsRoot() {
		return roofPartsRoot;
	}

	@Override
	protected String getTextureFileName() {
		if (Scene.getInstance().getTextureMode() == TextureMode.None) { // backward compatibility
			return null;
		}
		if (Scene.getInstance().getTextureMode() == TextureMode.Simple) { // backward compatibility
			return "roof.png";
		}
		switch (textureType) {
		case TEXTURE_01:
			return "roof_01.png";
		case TEXTURE_02:
			return "roof_02.png";
		case TEXTURE_03:
			return "roof_03.png";
		case TEXTURE_04:
			return "roof_04.png";
		case TEXTURE_05:
			return "roof_05.png";
		case TEXTURE_06:
			return "roof_06.png";
		case TEXTURE_07:
			return "roof_07.png";
		}
		return null;
	}

	protected abstract void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints);

	private void applyOverhang(final List<ReadOnlyVector3> wallUpperPoints, final List<ReadOnlyVector3> wallNormals) {
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			final Vector3 overhang = wallNormals.get(i).multiply(overhangLength, null);
			wallUpperPoints.set(i, overhang.addLocal(wallUpperPoints.get(i)));
		}
	}

	public void setGable(final int roofPartIndex, final boolean isGable, final UndoManager undoManager) {
		final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints = new ArrayList<ReadOnlyVector3>();
		final Wall wall = findGableWall(roofPartIndex, roofPartMeshUpperPoints);
		if (wall != null) {
			if (undoManager != null) {
				undoManager.addEdit(new MakeGableCommand(this, wall, roofPartMeshUpperPoints));
			}
			setGable(wall, isGable, true, roofPartMeshUpperPoints);
		}
	}

	public Wall findGableWall(final int roofPartIndex, final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints) {
		final ReadOnlyVector3[] roofMeshBase = findBasePoints((Mesh) ((Node) getRoofPartsRoot().getChild(roofPartIndex)).getChild(0), roofPartMeshUpperPoints);
		if (roofMeshBase == null) {
			return null;
		} else {
			return findGableWall(roofMeshBase[0], roofMeshBase[1]);
		}
	}

	public void setGable(final Wall wall, final boolean isGable, final boolean redraw, final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints) {
		System.out.println("setGable(" + wall + ", " + isGable + ")");
		if (gableEditPointToWallMap == null) {
			gableEditPointToWallMap = new HashMap<Integer, List<Wall>>();
		}

		if (isGable) {
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
				if (gableEditPointToWallMap.get(nearestEditPointIndex) == null) {
					gableEditPointToWallMap.put(nearestEditPointIndex, new ArrayList<Wall>(2));
				}
				gableEditPointToWallMap.get(nearestEditPointIndex).add(wall);
			}
		} else {
			final List<Integer> toBeRemoved = new ArrayList<Integer>();
			for (final Entry<Integer, List<Wall>> entry : gableEditPointToWallMap.entrySet()) {
				final List<Wall> value = entry.getValue();
				if (value.contains(wall)) {
					value.remove(wall);
					if (value.isEmpty()) {
						toBeRemoved.add(entry.getKey());
					}
				}
			}
			for (final int removedEditPoint : toBeRemoved) {
				gableEditPointToWallMap.remove(removedEditPoint);
			}
		}

		if (redraw) {
			draw();
			drawWalls();
		}
	}

	public void removeAllGables() {
		for (final List<Wall> walls : gableEditPointToWallMap.values()) {
			for (final Wall wall : walls) {
				setGable(wall, false, false, null);
			}
		}
		draw();
		drawWalls();
	}

	private void computeGableEditPoints() {
		if (gableEditPointToWallMap == null) {
			return;
		}
		for (final int editPointIndex : gableEditPointToWallMap.keySet()) {
			// if (editPointIndex >= points.size()) {
			// gableEditPointToWallMap.clear();
			// break;
			// }
			final Vector3 editPoint = getAbsPoint(editPointIndex);
			final List<Wall> gableWalls = gableEditPointToWallMap.get(editPointIndex);
			final List<ReadOnlyVector3> wallPoints = new ArrayList<ReadOnlyVector3>(gableWalls.size() * 2);
			final List<ReadOnlyVector3> wallNormals = new ArrayList<ReadOnlyVector3>(gableWalls.size() * 2);
			for (final Wall wall : gableWalls) {
				addPointToPolygon(wall.getAbsPoint(0), wall.getNormal(), wallPoints, wallNormals);
				addPointToPolygon(wall.getAbsPoint(2), wall.getNormal(), wallPoints, wallNormals);
			}
			applyOverhang(wallPoints, wallNormals);
			if (gableWalls.size() == 1) {
				final ReadOnlyVector2 p2D = Util.snapToPolygon(editPoint, wallPoints, wallNormals);
				editPoint.setX(p2D.getX());
				editPoint.setY(p2D.getY());
			} else if (gableWalls.size() > 1) {
				final Vector3 p0 = gableWalls.get(0).getAbsPoint(0);
				final Wall secondWall = gableWalls.get(1);
				final ReadOnlyVector3 cornerPoint;
				final int cornerIndex;
				if (Util.isEqual(p0, secondWall.getAbsPoint(0)) || Util.isEqual(p0, secondWall.getAbsPoint(2))) {
					cornerIndex = 0;
				} else {
					cornerIndex = 1;
				}
				cornerPoint = wallPoints.get(cornerIndex).subtract(wallNormals.get(cornerIndex).multiply(0.01, null), null);
				editPoint.setX(cornerPoint.getX());
				editPoint.setY(cornerPoint.getY());
			}
			points.get(editPointIndex).set(toRelative(editPoint));
		}
	}

	private void ensureEditPointsInside() {
		for (int i = 1; i < points.size(); i++) {
			final Vector3 editPoint = getAbsPoint(i);
			final Vector2 p = new Vector2(editPoint.getX(), editPoint.getY());

			if (!insideWallsPolygon(editPoint)) {
				double closestDistance = Double.MAX_VALUE;
				int closestIndex = 0;
				for (int j = 0; j < wallUpperPoints.size(); j++) {
					final Vector2 l1 = new Vector2(wallUpperPoints.get(j).getX(), wallUpperPoints.get(j).getY());
					final Vector2 l2 = new Vector2(wallUpperPoints.get((j + 1) % wallUpperPoints.size()).getX(), wallUpperPoints.get((j + 1) % wallUpperPoints.size()).getY());
					final double distance = p.distance(l1) + p.distance(l2);
					if (distance < closestDistance) {
						closestDistance = distance;
						closestIndex = j;
					}
				}
				final List<ReadOnlyVector3> wallPoints = new ArrayList<ReadOnlyVector3>(2);
				wallPoints.add(wallUpperPoints.get(closestIndex));
				wallPoints.add(wallUpperPoints.get((closestIndex + 1) % wallUpperPoints.size()));
				final ReadOnlyVector2 p2D = Util.snapToPolygon(editPoint, wallPoints, null);
				editPoint.setX(p2D.getX());
				editPoint.setY(p2D.getY());
				if (closestIndex < walls.size()) {
					editPoint.subtractLocal(walls.get(closestIndex).getNormal().multiply(0.01, null));
				}
				points.get(i).set(toRelative(editPoint));
			}
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
			if (meshPoint.equals(base[0]) || meshPoint.equals(base[1])) {
				continue;
			}

			if (meshPoint.getZ() > highestZ) {
				highestZ = meshPoint.getZ();
			}

			if (storeUpperPoints != null && !storeUpperPoints.contains(meshPoint)) {
				storeUpperPoints.add(meshPoint);
			}

			if (meshPoint.getZ() < base[0].getZ()) {
				final ReadOnlyVector3 tmp = base[0];
				base[0] = meshPoint;
				meshPoint = tmp;
			}
			if (meshPoint.getZ() < base[1].getZ()) {
				base[1] = meshPoint;
			}
		}

		if (highestZ == base[0].getZ() || highestZ == base[1].getZ()) {
			return null;
		}

		if (storeUpperPoints != null) {
			storeUpperPoints.remove(base[0]);
			storeUpperPoints.remove(base[1]);
		}
		return base;
	}

	private void hideGableRoofParts() {
		if (gableEditPointToWallMap == null) {
			return;
		}

		// Two Options: hide using estimating direction with wall. Or, hide using roof part number (it be wrong))
		for (final List<Wall> walls : gableEditPointToWallMap.values()) {
			for (final HousePart wall : walls) {
				final Vector3[] base_i = { wall.getAbsPoint(0), wall.getAbsPoint(2) };
				for (final Spatial roofPart : getRoofPartsRoot().getChildren()) {
					final ReadOnlyVector3[] base = findBasePoints((Mesh) ((Node) roofPart).getChild(0), null);
					if (base != null && isSameBasePoints(base_i[0], base_i[1], base[0], base[1])) {
						roofPart.getSceneHints().setCullHint(CullHint.Always);
						roofPart.getSceneHints().setAllPickingHints(false);
						break;
					}
				}
			}
		}
	}

	public boolean isSameBasePoints(final ReadOnlyVector3 a1, final ReadOnlyVector3 a2, final ReadOnlyVector3 b1, final ReadOnlyVector3 b2) {
		final double maxOverhangDistance = MathUtils.sqrt(2 * overhangLength * overhangLength) * 2;
		final Vector2 p1a = new Vector2(a1.getX(), a1.getY());
		final Vector2 p1b = new Vector2(a2.getX(), a2.getY());
		final Vector2 p2a = new Vector2(b1.getX(), b1.getY());
		final Vector2 p2b = new Vector2(b2.getX(), b2.getY());
		return (p1a.distance(p2a) <= maxOverhangDistance && p1b.distance(p2b) <= maxOverhangDistance) || (p1a.distance(p2b) <= maxOverhangDistance && p1b.distance(p2a) <= maxOverhangDistance);
	}

	protected Wall findGableWall(final ReadOnlyVector3 targetBase1, final ReadOnlyVector3 targetBase2) {
		for (final Wall wall : walls) {
			if (isSameBasePoints(targetBase1, targetBase2, wall.getAbsPoint(0), wall.getAbsPoint(2))) {
				return wall;
			}
		}
		return null;
	}

	@Override
	public void delete() {
		super.delete();
		for (final Wall wall : walls) {
			// if the wall doesn't already have another roof on top of it (it's possible when the user replaces an old roof with a new roof)
			if (wall.getRoof() == this) {
				wall.setRoof(null);
			}
			wall.draw();
		}
	}

	@Override
	public void setOriginal(final HousePart original) {
		final Roof originalRoof = (Roof) original;
		this.original = original;
		root.detachChild(pointsRoot);
		root.detachChild(roofPartsRoot);
		roofPartsRoot = originalRoof.roofPartsRoot.makeCopy(true);
		root.attachChild(roofPartsRoot);

		for (int i = 0; i < roofPartsRoot.getNumberOfChildren(); i++) {
			final UserData orgUserData = (UserData) ((Node) originalRoof.roofPartsRoot.getChild(i)).getChild(0).getUserData();
			final Mesh mesh = (Mesh) ((Node) roofPartsRoot.getChild(i)).getChild(0);
			mesh.setUserData(new UserData(this, orgUserData.getEditPointIndex(), false));
			roofPartsRoot.getChild(i).setUserData(originalRoof.roofPartsRoot.getChild(i).getUserData());
			final Line outlineMesh = (Line) ((Node) roofPartsRoot.getChild(i)).getChild(4);
			outlineMesh.setLineWidth(printOutlineThickness);
		}
		drawAnnotations();
		root.updateWorldBound(true);
	}

	@Override
	protected Vector3 getCenter() {
		final Vector3 min = new Vector3(wallUpperPoints.get(0));
		final Vector3 max = new Vector3(wallUpperPoints.get(0));
		for (final ReadOnlyVector3 p : wallUpperPoints) {
			min.setX(Math.min(min.getX(), p.getX()));
			min.setY(Math.min(min.getY(), p.getY()));
			max.setX(Math.max(max.getX(), p.getX()));
			max.setY(Math.max(max.getY(), p.getY()));
		}
		return min.addLocal(max).multiplyLocal(new Vector3(0.5, 0.5, 0)).addLocal(0, 0, container.getPoints().get(1).getZ());
	}

	@Override
	public void setAnnotationsVisible(final boolean visible) {
		super.setAnnotationsVisible(visible);
		final CullHint cull = visible ? CullHint.Inherit : CullHint.Always;
		if (roofPartsRoot != null) {
			for (final Spatial roofPart : roofPartsRoot.getChildren()) {
				if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
					((Node) roofPart).getChild(1).getSceneHints().setCullHint(cull);
					((Node) roofPart).getChild(2).getSceneHints().setCullHint(cull);
				}
			}
		}
	}

	@Override
	public void drawGrids(final double gridSize) {
		final BoundingBox bounds = (BoundingBox) root.getWorldBound().asType(Type.AABB);
		final ReadOnlyVector3 width = Vector3.UNIT_X.multiply(bounds.getXExtent() * 2, null);
		final ReadOnlyVector3 height = Vector3.UNIT_Y.multiply(bounds.getYExtent() * 2, null);
		final ArrayList<ReadOnlyVector3> points = new ArrayList<ReadOnlyVector3>();
		final ReadOnlyVector3 center = getCenter();
		final ReadOnlyVector3 pMiddle = new Vector3(center.getX(), center.getY(), getAbsPoint(0).getZ());

		final int cols = (int) (width.length() / gridSize);

		for (int col = 0; col < cols / 2 + 1; col++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = width.normalize(null).multiplyLocal(neg * col * gridSize).addLocal(pMiddle).subtractLocal(height.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(height, null);
				points.add(lineP2);
				if (col == 0) {
					break;
				}
			}
		}

		final int rows = (int) (height.length() / gridSize);

		for (int row = 0; row < rows / 2 + 1; row++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = height.normalize(null).multiplyLocal(neg * row * gridSize).addLocal(pMiddle).subtractLocal(width.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(width, null);
				points.add(lineP2);
				if (row == 0) {
					break;
				}
			}
		}
		if (points.size() < 2) {
			return;
		}
		final FloatBuffer buf = BufferUtils.createVector3Buffer(points.size());
		for (final ReadOnlyVector3 p : points) {
			buf.put(p.getXf()).put(p.getYf()).put(pMiddle.getZf());
		}

		gridsMesh.getMeshData().setVertexBuffer(buf);
	}

	public void setPrintVertical(final Spatial roofPartNode, final boolean isVertical) {
		roofPartPrintVerticalMap.put(roofPartNode, isVertical);
		flattenQuadTriangle((Node) roofPartNode, 1.0);
		roofPartNode.updateGeometricState(0);
		final Mesh roofPartMesh = (Mesh) ((Node) roofPartNode).getChild(0);
		final ReadOnlyVector3 center = computeOrientedBoundingBox(roofPartMesh);
		orgCenters.put((Node) roofPartNode, center);
	}

	protected void snapToWallsPolygon(final Vector3 p) {
		if (!insideWallsPolygon(p)) {
			final ReadOnlyVector2 p2D = Util.snapToPolygon(p, wallUpperPoints, wallNormals);
			p.set(p2D.getX(), p2D.getY(), p.getZ());
		}
	}

	protected boolean insideWallsPolygon(final Vector3 p) {
		return Util.insidePolygon(p, wallUpperPoints);
	}

	public void computeHeight(final List<? extends ReadOnlyVector3> wallUpperPoints) {
		double maxZ = 0;
		for (final ReadOnlyVector3 p : wallUpperPoints) {
			maxZ = Math.max(maxZ, p.getZ());
		}
		// to make height relative to container wall so that applyHeight() runs the same way
		height = 15.0 + maxZ - container.getPoints().get(1).getZ();
		// height = height + maxZ - container.getPoints().get(1).getZ();
		// avoid this because it will result in increasing height when hovering
		// mouse from one wall contain to another before fully inserting the roof
	}

	public void applyHeight() {
		for (final Vector3 p : points) {
			p.setZ(container.getPoints().get(1).getZ() + height);
		}
	}

	protected void postEdit(final EditState editState) {
		draw();
		drawWalls();

		if (editState.fitTestRequired && !windowsFit()) {
			for (int i = 0; i < points.size(); i++) {
				points.get(i).set(editState.orgPoints.get(i));
			}
			height = editState.orgHeight;
			draw();
			drawWalls();
		}

		drawChildren();
		drawOutline(); // because drawing children will extend their edit points to roof

		if (container != null) {
			setEditPointsVisible(true);
		}
	}

	@Override
	protected void computeArea() {
		this.area = 0;
		if (container == null) {
			return;
		}

		if (areaByPartWithOverhang == null) {
			areaByPartWithOverhang = new HashMap<Mesh, Double>();
		} else {
			areaByPartWithOverhang.clear();
		}

		if (areaByPartWithoutOverhang == null) {
			areaByPartWithoutOverhang = new HashMap<Mesh, Double>();
		} else {
			areaByPartWithoutOverhang.clear();
		}

		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Node roofPartNode = (Node) roofPart;
				final Mesh roofPartMesh = (Mesh) roofPartNode.getChild(REAL_MESH_INDEX);
				areaByPartWithOverhang.put(roofPartMesh, Util.computeArea(roofPartMesh));
				final FloatBuffer vertexBuffer = roofPartMesh.getMeshData().getVertexBuffer();
				final Vector3 p = new Vector3();
				if (overhangLength <= OVERHANG_MIN) {
					final double a = Util.computeArea(roofPartMesh);
					areaByPartWithoutOverhang.put(roofPartMesh, a);
					area += a;
				} else {
					final List<ReadOnlyVector3> result = computeDashPoints(roofPartMesh);
					if (result.isEmpty()) {
						vertexBuffer.rewind();
						p.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
						final double a;
						if (Util.insidePolygon(p, wallUpperPointsWithoutOverhang)) {
							a = Util.computeArea(roofPartMesh);
						} else {
							a = 0;
						}
						areaByPartWithoutOverhang.put(roofPartMesh, a);
						area += a;
					} else {
						// if (roofPartsRoot.getNumberOfChildren() > 1) {
						double highPointZ = Double.NEGATIVE_INFINITY;
						vertexBuffer.rewind();
						while (vertexBuffer.hasRemaining()) {
							p.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
							if (p.getZ() > highPointZ) {
								highPointZ = p.getZ();
							}
						}

						final List<ReadOnlyVector3> highPoints = new ArrayList<ReadOnlyVector3>();
						vertexBuffer.rewind();
						while (vertexBuffer.hasRemaining()) {
							p.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
							if (p.getZ() >= highPointZ - MathUtils.ZERO_TOLERANCE && Util.insidePolygon(p, wallUpperPointsWithoutOverhang)) {
								highPoints.add(new Vector3(p));
							}
						}

						if (highPoints.size() == 1) {
							result.add(highPoints.get(0));
						} else {
							final ReadOnlyVector3 lastPoint = result.get(result.size() - 1);
							while (!highPoints.isEmpty()) {
								double shortestDistance = Double.MAX_VALUE;
								ReadOnlyVector3 nearestPoint = null;
								for (final ReadOnlyVector3 hp : highPoints) {
									final double distance = hp.distance(lastPoint);
									if (distance < shortestDistance) {
										shortestDistance = distance;
										nearestPoint = hp;
									}
								}
								result.add(nearestPoint);
								highPoints.remove(nearestPoint);
							}
						}
						result.add(result.get(0));
						final double annotationScale = Scene.getInstance().getAnnotationScale();
						final double a = Util.area3D_Polygon(result, (ReadOnlyVector3) roofPart.getUserData()) * annotationScale * annotationScale;
						areaByPartWithoutOverhang.put(roofPartMesh, a);
						this.area += a;
					}
				}
			}
		}
	}

	// return the area WITH overhang
	@Override
	public double getArea() {
		if (areaByPartWithOverhang == null) {
			return 0;
		}
		double a = 0;
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Node roofPartNode = (Node) roofPart;
				final Mesh roofPartMesh = (Mesh) roofPartNode.getChild(REAL_MESH_INDEX);
				final Double d = areaByPartWithOverhang.get(roofPartMesh);
				if (d != null) {
					a += d;
				}
			}
		}
		return a;
	}

	public double getAreaWithoutOverhang(final Mesh mesh) {
		if (areaByPartWithoutOverhang == null) {
			return 0;
		}
		final Double d = areaByPartWithoutOverhang.get(mesh);
		if (d == null) {
			return 0;
		}
		return d;
	}

	// if wallUpperPoints is null then it has not been drawn yet so we assume wallUpperPoints size is okay otherwise all roofs would be invalid at init time
	@Override
	public boolean isDrawable() {
		return container != null && (wallUpperPoints == null || wallUpperPoints.size() >= 3);
	}

	@Override
	protected HousePart getContainerRelative() {
		return getTopContainer();
	}

	@Override
	public Spatial getCollisionSpatial() {
		return roofPartsRoot;
	}

	public boolean insideWalls(final double x, final double y, final boolean init) {
		if (walls.isEmpty()) {
			return false;
		}
		if (init) {
			if (underlyingWallVerticesOnFoundation == null) {
				underlyingWallVerticesOnFoundation = new ArrayList<Vector2>();
			} else {
				underlyingWallVerticesOnFoundation.clear();
			}
			walls.get(0).visitNeighbors(new WallVisitor() {
				@Override
				public void visit(final Wall currentWall, final Snap prev, final Snap next) {
					int pointIndex = 0;
					if (next != null) {
						pointIndex = next.getSnapPointIndexOf(currentWall);
					}
					pointIndex++;
					addUnderlyingWallVertex(currentWall.getAbsPoint(pointIndex == 1 ? 3 : 1));
					addUnderlyingWallVertex(currentWall.getAbsPoint(pointIndex));
				}

				private void addUnderlyingWallVertex(final ReadOnlyVector3 v3) {
					final Vector2 v2 = new Vector2(v3.getX(), v3.getY());
					boolean b = false;
					for (final Vector2 x : underlyingWallVerticesOnFoundation) {
						if (Util.isEqual(x, v2)) {
							b = true;
							break;
						}
					}
					if (!b) {
						underlyingWallVerticesOnFoundation.add(v2);
					}
				}
			});
			if (underlyingWallPath == null) {
				underlyingWallPath = new Path2D.Double();
			} else {
				underlyingWallPath.reset();
			}
			final Vector2 v0 = underlyingWallVerticesOnFoundation.get(0);
			underlyingWallPath.moveTo(v0.getX(), v0.getY());
			for (int i = 1; i < underlyingWallVerticesOnFoundation.size(); i++) {
				final Vector2 v = underlyingWallVerticesOnFoundation.get(i);
				underlyingWallPath.lineTo(v.getX(), v.getY());
			}
			underlyingWallPath.lineTo(v0.getX(), v0.getY());
			underlyingWallPath.closePath();
		}
		return underlyingWallPath != null ? underlyingWallPath.contains(x, y) : false;
	}

	private double calculateHeatVector(final Mesh mesh) {
		double heat = 0;
		final double[] heatLossArray = SolarRadiation.getInstance().getHeatLoss(mesh);
		if (heatLossArray != null) {
			if (SceneManager.getInstance().isHeatFluxDaily()) {
				for (final double x : heatLossArray) {
					heat += x;
				}
				heat /= getAreaWithoutOverhang(mesh) * heatLossArray.length;
				heatFlux.setDefaultColor(ColorRGBA.YELLOW);
			} else {
				final int hourOfDay = Heliodon.getInstance().getCalendar().get(Calendar.HOUR_OF_DAY);
				heat = heatLossArray[hourOfDay * 4] + heatLossArray[hourOfDay * 4 + 1] + heatLossArray[hourOfDay * 4 + 2] + heatLossArray[hourOfDay * 4 + 3];
				heat /= 4 * getAreaWithoutOverhang(mesh);
				heatFlux.setDefaultColor(ColorRGBA.WHITE);
			}
		}
		return heat;
	}

	@Override
	public void drawHeatFlux() {
		FloatBuffer arrowsVertices = heatFlux.getMeshData().getVertexBuffer();
		final Foundation foundation = getTopContainer();
		final int cols = (int) Math.max(2, foundation.getAbsPoint(0).distance(foundation.getAbsPoint(2)) / Scene.getInstance().getHeatVectorGridSize());
		final int rows = (int) Math.max(2, foundation.getAbsPoint(0).distance(foundation.getAbsPoint(1)) / Scene.getInstance().getHeatVectorGridSize());
		arrowsVertices = BufferUtils.createVector3Buffer(rows * cols * 6);
		heatFlux.getMeshData().setVertexBuffer(arrowsVertices);
		final ReadOnlyVector3 o = foundation.getAbsPoint(0);
		final ReadOnlyVector3 u = foundation.getAbsPoint(2).subtract(o, null);
		final ReadOnlyVector3 v = foundation.getAbsPoint(1).subtract(o, null);
		final Vector3 a = new Vector3();
		double g, h;
		boolean init = true;
		for (int j = 0; j < cols; j++) {
			h = j + 0.5;
			for (int i = 0; i < rows; i++) {
				g = i + 0.5;
				a.setX(o.getX() + g * v.getX() / rows + h * u.getX() / cols);
				a.setY(o.getY() + g * v.getY() / rows + h * u.getY() / cols);
				a.setZ(o.getZ());
				if (insideWalls(a.getX(), a.getY(), init)) {
					ReadOnlyVector3 b = null;
					Node node = null;
					Mesh mesh = null;
					for (final Spatial child : roofPartsRoot.getChildren()) {
						if (child.getSceneHints().getCullHint() != CullHint.Always) {
							node = (Node) child;
							mesh = (Mesh) node.getChild(REAL_MESH_INDEX);
							b = findRoofIntersection(mesh, a);
							if (b != null) {
								break;
							}
						}
					}
					if (b != null) {
						final ReadOnlyVector3 normal = (ReadOnlyVector3) node.getUserData();
						final double heat = calculateHeatVector(mesh);
						drawArrow(b, normal, arrowsVertices, heat);
					}
				}
				if (init) {
					init = false;
				}
			}
			heatFlux.getMeshData().updateVertexCount();
			heatFlux.updateModelBound();
		}
		updateHeatFluxVisibility();
	}

	@Override
	public String toString() {
		String s = this.getClass().getSimpleName() + "(" + id + ")";
		s += ("  editPoint=" + editPointIndex);
		return s;
	}

	public void remove(final Wall wall) {
		if (gableEditPointToWallMap != null) {
			for (final List<Wall> wallList : gableEditPointToWallMap.values()) {
				wallList.remove(wall);
			}
		}
	}

	@Override
	public boolean isCopyable() {
		return false;
	}

	public double getOverhangLength() {
		return overhangLength;
	}

	public void setOverhangLength(final double overhangLength) {
		this.overhangLength = overhangLength;
	}

	@Override
	public void setUValue(final double uValue) {
		this.uValue = uValue;
	}

	@Override
	public double getUValue() {
		return uValue;
	}

	@Override
	public void setVolumetricHeatCapacity(final double volumetricHeatCapacity) {
		this.volumetricHeatCapacity = volumetricHeatCapacity;
	}

	@Override
	public double getVolumetricHeatCapacity() {
		return volumetricHeatCapacity;
	}

	public List<Window> getWindows() {
		final List<Window> windows = new ArrayList<Window>();
		for (final HousePart x : children) {
			if (x instanceof Window) {
				windows.add((Window) x);
			}
		}
		return windows;
	}

	@Override
	protected boolean fits(final HousePart window) {
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
				final ArrayList<ReadOnlyVector3> outlinePoints = MeshLib.computeOutline(mesh.getMeshData().getVertexBuffer());

				boolean allInside = true;
				boolean allOutside = true;
				for (int i = 0; i < window.getPoints().size(); i++) {
					final Vector3 p = window.getAbsPoint(i);
					roofPart.getWorldTransform().applyInverse(p);
					if (Util.insidePolygon(p, outlinePoints)) {
						allOutside = false;
					} else {
						allInside = false;
					}
				}
				if (!allInside && !allOutside) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isSingleFlatMesh() {
		return getRoofPartsRoot().getNumberOfChildren() == 1 && Util.isEqual((ReadOnlyVector3) getRoofPartsRoot().getChild(0).getUserData(), Vector3.UNIT_Z, 0.01);
	}

	@Override
	public void addPrintMeshes(final List<Mesh> list) {
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Mesh mesh = (Mesh) ((Node) roofPart).getChild(REAL_MESH_INDEX);
				addPrintMesh(list, mesh);
			}
		}
	}

	public void setType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
