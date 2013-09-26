package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.swing.undo.UndoManager;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.undo.MakeGableCommand;
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

public abstract class Roof extends HousePart {
	private static final long serialVersionUID = 1L;
	protected transient Node roofPartsRoot;
	private transient Map<Spatial, Boolean> roofPartPrintVerticalMap;
	private transient Map<Node, ReadOnlyVector3> orgCenters;
	private transient List<ReadOnlyVector3> wallUpperPoints;
	private transient List<ReadOnlyVector3> wallNormals;
	private transient List<Wall> walls;
	private transient HousePart previousContainer;
	private Map<Integer, List<Wall>> gableEditPointToWallMap = null;

	protected class EditState {
		final boolean fitTestRequired;
		final ArrayList<Vector3> orgPoints;
		final double orgHeight;

		public EditState() {
			if (editPointIndex == -1)
				orgPoints = null;
			else {
				orgPoints = new ArrayList<Vector3>(points.size());
				for (final Vector3 v : points)
					orgPoints.add(v.clone());
			}
			if (editPointIndex == -1 || !windowsFit())
				fitTestRequired = false;
			else
				fitTestRequired = true;
			orgHeight = height;
		}
	}

	public Roof(final int numOfEditPoints) {
		super(1, numOfEditPoints, 5.0);
	}

	@Override
	protected void init() {
		super.init();
		relativeToHorizontal = true;
		orgCenters = new HashMap<Node, ReadOnlyVector3>();
		wallNormals = new ArrayList<ReadOnlyVector3>();
		walls = new ArrayList<Wall>();
		roofPartPrintVerticalMap = new HashMap<Spatial, Boolean>();

		roofPartsRoot = new Node("Roof Meshes Root");
		root.attachChild(roofPartsRoot);

		mesh = new Mesh("Roof");
		mesh.setModelBound(null);

		getEditPointShape(0).setDefaultColor(ColorRGBA.CYAN);
	}

	@Override
	protected void drawMesh() {
		/* undo the effect of wall stretch on all walls if roof is moved to new walls */
		if (previousContainer != container) {
			previousContainer = container;
			for (final Wall wall : walls) {
				wall.setRoof(null);
				wall.draw();
			}
		}

		if (wallUpperPoints == null)
			wallUpperPoints = new ArrayList<ReadOnlyVector3>();
		else
			wallUpperPoints.clear();
		if (container != null)
			initWallUpperPoints((Wall) container, walls, wallUpperPoints, wallNormals);
		if (!isDrawable()) {
			roofPartsRoot.getSceneHints().setCullHint(CullHint.Always);
			setEditPointsVisible(false);
			return;
		}
		roofPartsRoot.getSceneHints().setCullHint(CullHint.Inherit);

		applyOverhang(wallUpperPoints, wallNormals);
		processRoofEditPoints(wallUpperPoints);
		computeGableEditPoints();
		final Polygon polygon = makePolygon(wallUpperPoints);
		applySteinerPoint(polygon);
		MeshLib.fillMeshWithPolygon(mesh, polygon, null, true, null, null, null);
		MeshLib.groupByPlanner(mesh, roofPartsRoot);
		setAnnotationsVisible(Scene.getInstance().isAnnotationsVisible());
		hideGableRoofParts();
		int roofPartIndex = 0;
		for (final Spatial child : roofPartsRoot.getChildren()) {
			final Mesh mesh = (Mesh) ((Node) child).getChild(0);
			mesh.setUserData(new UserData(this, roofPartIndex, false));
			roofPartIndex++;
		}
		drawWireframe();
		drawDashLines();
	}

	protected void drawWalls() {
		if (container != null)
			((Wall) container).visitNeighbors(new WallVisitor() {
				@Override
				public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
					currentWall.draw();
				}
			});
	}

	protected boolean windowsFit() {
		for (final Wall wall : walls)
			if (!wall.windowsFit())
				return false;
		return true;
	}

	private void drawDashLines() {
		if (container == null)
			return;

		if (isFrozen()) {
			for (final Spatial roofPart : roofPartsRoot.getChildren()) {
				final Node roofPartNode = (Node) roofPart;
				final Mesh dashLinesMesh = (Mesh) roofPartNode.getChild(5);
				dashLinesMesh.setVisible(false);
			}
		} else
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					for (final Spatial roofPart : roofPartsRoot.getChildren()) {
						final Node roofPartNode = (Node) roofPart;
						final Mesh dashLinesMesh = (Mesh) roofPartNode.getChild(5);
						final ArrayList<ReadOnlyVector3> result = new ArrayList<ReadOnlyVector3>();
						((Wall) container).visitNeighbors(new WallVisitor() {
							@Override
							public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
								stretchToRoof(result, (Mesh) roofPartNode.getChild(0), currentWall.getAbsPoint(0), currentWall.getAbsPoint(2));
							}
						});
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

							for (final ReadOnlyVector3 p : result)
								vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

							dashLinesMesh.getMeshData().updateVertexCount();
							dashLinesMesh.updateModelBound();
						}
					}
					updateDashLinesColor();
					return null;
				}
			});
	}

	public void updateDashLinesColor() {
		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			final Node roofPartNode = (Node) roofPart;
			final Mesh dashLinesMesh = (Mesh) roofPartNode.getChild(5);
			dashLinesMesh.setDefaultColor(ColorRGBA.RED);
		}
	}

	private void stretchToRoof(final ArrayList<ReadOnlyVector3> result, final Mesh roof, final ReadOnlyVector3 p1, final ReadOnlyVector3 p2) {
		final Vector3 dir = p2.subtract(p1, null).multiplyLocal(1, 1, 0);
		final double length = dir.length();
		dir.normalizeLocal();

		Vector3 direction = null;
		ReadOnlyVector3 previousStretchPoint = null;
		boolean firstInsert = false;

		final double step = 0.1;
		for (double d = length; d > step; d -= step) {
			final Vector3 p = dir.multiply(d, null).addLocal(p1);
			final ReadOnlyVector3 currentStretchPoint = findRoofIntersection(roof, p);

			if (currentStretchPoint != null && !firstInsert) {
				result.add(currentStretchPoint);
				firstInsert = true;
			} else if (currentStretchPoint == null) {
				if (previousStretchPoint != null)
					result.add(previousStretchPoint);
				direction = null;
				firstInsert = false;
			} else {
				final Vector3 currentDirection = currentStretchPoint.subtract(previousStretchPoint, null).normalizeLocal();
				if (direction == null) {
					direction = currentDirection;
				} else if (direction.dot(currentDirection) < 1.0 - MathUtils.ZERO_TOLERANCE) {
					direction = null;
					result.add(currentStretchPoint);
				}
			}
			previousStretchPoint = currentStretchPoint;
		}

		if (previousStretchPoint != null)
			result.add(previousStretchPoint);
	}

	public ReadOnlyVector3 findRoofIntersection(final Mesh roofPart, final ReadOnlyVector3 p) {
		final double offset = 0.001;
		final PickResults pickResults = new PrimitivePickResults();
		PickingUtil.findPick(roofPart, new Ray3(p, Vector3.UNIT_Z), pickResults);
		if (pickResults.getNumber() > 0) {
			return pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).add(0, 0, offset, null);
		} else
			return null;
	}

	protected void fillMeshWithPolygon(final Mesh mesh, final Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			System.out.println("Triangulate exception received with the following polygon:");
			for (final TriangulationPoint p : polygon.getPoints())
				System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
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
					final int pointIndex2;
					if (nextSnap != null)
						pointIndex2 = nextSnap.getSnapPointIndexOf(currentWall) + 1;
					else
						pointIndex2 = 0 + 1;
					final int pointIndex1 = pointIndex2 == 1 ? 3 : 1;
					final Vector3 p1 = currentWall.getAbsPoint(pointIndex1);
					final Vector3 p2 = currentWall.getAbsPoint(pointIndex2);
					final ReadOnlyVector3 normal = currentWall.getFaceDirection();
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

	private Polygon makePolygon(final List<ReadOnlyVector3> wallUpperPoints) {
		final List<PolygonPoint> polygonPoints = new ArrayList<PolygonPoint>(wallUpperPoints.size());
		for (final ReadOnlyVector3 p : wallUpperPoints)
			polygonPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		return new Polygon(polygonPoints);
	}

	protected abstract Polygon applySteinerPoint(final Polygon polygon);

	@Override
	public void computeOrientedBoundingBox() {
		orgCenters.clear();

		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			final Mesh roofPartMesh = (Mesh) ((Node) roofPartNode).getChild(0);
			computeOrientedBoundingBox(roofPartMesh);
			orgCenters.put((Node) roofPartNode, new Vector3(roofPartMesh.getWorldBound().getCenter()));
		}
	}

	@Override
	public void flattenInit() {
		flatten(1.0);
		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			roofPartNode.setTranslation(0, 0, 0);

			final Mesh mesh = (Mesh) ((Node) roofPartNode).getChild(0);
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

	@Override
	public void flatten(final double flattenTime) {
		for (final Spatial child : getRoofPartsRoot().getChildren()) {
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

		if (orgCenter == null)
			orgCenter = Vector3.ZERO;
		final Vector3 targetPrintCenter = ((UserData) roofPartNode.getChild(0).getUserData()).getPrintCenter();
		if (!targetPrintCenter.equals(Vector3.ZERO))
			roofPartNode.setTranslation(targetPrintCenter.subtract(orgCenter, null).multiplyLocal(flattenTime));
	}

	@Override
	protected void clearAnnotations() {
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

		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
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
				if (drawAnnotationsInside)
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

	protected void drawWireframe() {
		if (container == null)
			return;

		for (final Spatial roofPart : roofPartsRoot.getChildren()) {
			final Node roofPartNode = (Node) roofPart;
			final Mesh wireframeMesh = (Mesh) roofPartNode.getChild(4);

			final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeOutline(((Mesh) roofPartNode.getChild(0)).getMeshData().getVertexBuffer());
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
		}
	}

	@Override
	public int drawLabels(int printSequence) {
		for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
			final String text = "(" + (printSequence++ + 1) + ")";
			final BMText label = (BMText) ((Node) roofPartNode).getChild(3);
			label.getSceneHints().setCullHint(CullHint.Inherit);
			label.setText(text);
		}
		return printSequence;
	}

	@Override
	public void hideLabels() {
		for (final Spatial roofPartNode : roofPartsRoot.getChildren())
			((Node) roofPartNode).getChild(3).getSceneHints().setCullHint(CullHint.Always);
	}

	@Override
	public void updateTextureAndColor() {
		if (roofPartsRoot != null) {
			for (final Spatial roofPartNode : roofPartsRoot.getChildren()) {
				updateTextureAndColor((Mesh) ((Node) roofPartNode).getChild(0), Scene.getInstance().getRoofColor());
			}
		}
	}

	public Node getRoofPartsRoot() {
		return roofPartsRoot;
	}

	@Override
	protected String getTextureFileName() {
		return Scene.getInstance().getTextureMode() == TextureMode.Simple ? "roof.png" : "roof.jpg";
	}

	protected abstract void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints);

	private void applyOverhang(final List<ReadOnlyVector3> wallUpperPoints, final List<ReadOnlyVector3> wallNormals) {
		final Vector3 op = new Vector3();
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			final ReadOnlyVector3 p = wallUpperPoints.get(i);
			op.set(wallNormals.get(i)).multiplyLocal(Scene.getInstance().getOverhangLength());			
			wallUpperPoints.set(i, p.add(op, null));
		}
	}

	public void setGable(final int roofPartIndex, final boolean isGable, final UndoManager undoManager) {
		final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints = new ArrayList<ReadOnlyVector3>();
		final Wall wall = findGableWall(roofPartIndex, roofPartMeshUpperPoints);
		undoManager.addEdit(new MakeGableCommand(this, wall, roofPartMeshUpperPoints));
		setGable(wall, isGable, true, roofPartMeshUpperPoints);
	}

	public Wall findGableWall(final int roofPartIndex, final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints) {
		final ReadOnlyVector3[] roofMeshBase = findBasePoints((Mesh) ((Node) getRoofPartsRoot().getChild(roofPartIndex)).getChild(0), roofPartMeshUpperPoints);
		return findGableWall(roofMeshBase[0], roofMeshBase[1]);
	}

	public void setGable(final Wall wall, final boolean isGable, final boolean redraw, final ArrayList<ReadOnlyVector3> roofPartMeshUpperPoints) {
		System.out.println("setGable(" + wall + ", " + isGable + ")");
		if (gableEditPointToWallMap == null)
			gableEditPointToWallMap = new HashMap<Integer, List<Wall>>();

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
				if (gableEditPointToWallMap.get(nearestEditPointIndex) == null)
					gableEditPointToWallMap.put(nearestEditPointIndex, new ArrayList<Wall>(2));
				gableEditPointToWallMap.get(nearestEditPointIndex).add(wall);
			}
		} else {
			final List<Integer> toBeRemoved = new ArrayList<Integer>();
			for (final Entry<Integer, List<Wall>> entry : gableEditPointToWallMap.entrySet()) {
				final List<Wall> value = entry.getValue();
				if (value.contains(wall)) {
					value.remove(wall);
					if (value.isEmpty())
						toBeRemoved.add(entry.getKey());
				}
			}
			for (final int removedEditPoint : toBeRemoved)
				gableEditPointToWallMap.remove(removedEditPoint);
		}

		if (redraw) {
			draw();
			drawWalls();
		}
	}

	public void removeAllGables() {
		for (final List<Wall> walls : gableEditPointToWallMap.values())
			for (final Wall wall : walls)
				setGable(wall, false, false, null);
		draw();
		drawWalls();
	}

	private void computeGableEditPoints() {
		if (gableEditPointToWallMap == null)
			return;
		for (final int editPointIndex : gableEditPointToWallMap.keySet()) {
			final Vector3 editPoint = getAbsPoint(editPointIndex);
			final List<Wall> gableWalls = gableEditPointToWallMap.get(editPointIndex);
			final List<ReadOnlyVector3> wallPoints = new ArrayList<ReadOnlyVector3>(gableWalls.size() * 2);
			final List<ReadOnlyVector3> wallNormals = new ArrayList<ReadOnlyVector3>(gableWalls.size() * 2);
			for (final HousePart wall : gableWalls) {
				addPointToPolygon(wall.getAbsPoint(0), wall.getFaceDirection(), wallPoints, wallNormals);
				addPointToPolygon(wall.getAbsPoint(2), wall.getFaceDirection(), wallPoints, wallNormals);
			}
			applyOverhang(wallPoints, wallNormals);
			if (gableWalls.size() == 1) {
				final ReadOnlyVector2 p2D = Util.snapToPolygon(editPoint, wallPoints, wallNormals);
				editPoint.setX(p2D.getX());
				editPoint.setY(p2D.getY());				
			} else {
				for (final ReadOnlyVector3 p1 : wallPoints)
					for (final ReadOnlyVector3 p2 : wallPoints)
						if (Util.isEqual(p1, p2))
							editPoint.set(p1);
			}
			points.get(editPointIndex).set(toRelative(editPoint, container.getContainer()));
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

			if (storeUpperPoints != null && !storeUpperPoints.contains(meshPoint))
				storeUpperPoints.add(meshPoint);

			if (meshPoint.getZ() < base[0].getZ()) {
				final ReadOnlyVector3 tmp = base[0];
				base[0] = meshPoint;
				meshPoint = tmp;
			}
			if (meshPoint.getZ() < base[1].getZ())
				base[1] = meshPoint;
		}

		if (highestZ == base[0].getZ() || highestZ == base[1].getZ())
			return null;

		if (storeUpperPoints != null) {
			storeUpperPoints.remove(base[0]);
			storeUpperPoints.remove(base[1]);
		}
		return base;
	}

	private void hideGableRoofParts() {
		if (gableEditPointToWallMap == null)
			return;

		/* Two Options: hide using estimating direction with wall. Or, hide using roof part number (it be wrong)) */
		for (final List<Wall> walls : gableEditPointToWallMap.values())
			for (final HousePart wall : walls) {
				final Vector3[] base_i = { wall.getAbsPoint(0), wall.getAbsPoint(2) };
				for (final Spatial roofPart : getRoofPartsRoot().getChildren()) {
					final ReadOnlyVector3[] base = findBasePoints((Mesh) ((Node) roofPart).getChild(0), null);
					if (base != null && isSameBasePoints(base_i[0], base_i[1], base[0], base[1])) {
						roofPart.removeFromParent();
						break;
					}
				}
			}
	}

	public boolean isSameBasePoints(final ReadOnlyVector3 a1, final ReadOnlyVector3 a2, final ReadOnlyVector3 b1, final ReadOnlyVector3 b2) {
		final double maxOverhangDistance = MathUtils.sqrt(2 * Scene.getInstance().getOverhangLength() * Scene.getInstance().getOverhangLength()) * 2;
		final Vector2 p1a = new Vector2(a1.getX(), a1.getY());
		final Vector2 p1b = new Vector2(a2.getX(), a2.getY());
		final Vector2 p2a = new Vector2(b1.getX(), b1.getY());
		final Vector2 p2b = new Vector2(b2.getX(), b2.getY());
		return (p1a.distance(p2a) <= maxOverhangDistance && p1b.distance(p2b) <= maxOverhangDistance) || (p1a.distance(p2b) <= maxOverhangDistance && p1b.distance(p2a) <= maxOverhangDistance);
	}

	protected Wall findGableWall(final ReadOnlyVector3 targetBase1, final ReadOnlyVector3 targetBase2) {
		for (final Wall wall : walls) {
			if (isSameBasePoints(targetBase1, targetBase2, wall.getAbsPoint(0), wall.getAbsPoint(2)))
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
					if (Util.isEqual(p, new Vector3(buf.get(), buf.get(), buf.get())))
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
			// if the wall doesn't already have another roof on top of it (it's possible when the user replaces an old roof with a new roof)
			if (wall.getRoof() == this)
				wall.setRoof(null);
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
			mesh.setUserData(new UserData(this, orgUserData.getIndex(), false));
			roofPartsRoot.getChild(i).setUserData(originalRoof.roofPartsRoot.getChild(i).getUserData());
			final Line wireframeMesh = (Line) ((Node) roofPartsRoot.getChild(i)).getChild(4);
			wireframeMesh.setLineWidth(printWireframeThickness);
		}
		drawAnnotations();
		root.updateWorldBound(true);
	}

	@Override
	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index), container == null ? null : container.getContainer());
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
		if (roofPartsRoot != null)
			for (final Spatial roofPart : roofPartsRoot.getChildren()) {
				((Node) roofPart).getChild(1).getSceneHints().setCullHint(cull);
				((Node) roofPart).getChild(2).getSceneHints().setCullHint(cull);
			}
	}

	@Override
	public void drawGrids(final double gridSize) {
		final BoundingBox bounds = (BoundingBox) root.getWorldBound();
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
			buf.put(p.getXf()).put(p.getYf()).put(pMiddle.getZf());

		gridsMesh.getMeshData().setVertexBuffer(buf);
	}

	public void setPrintVertical(final Spatial roofPartNode, final boolean isVertical) {
		roofPartPrintVerticalMap.put(roofPartNode, isVertical);
		flattenQuadTriangle((Node) roofPartNode, 1.0);
		roofPartNode.updateGeometricState(0);
		orgCenters.put((Node) roofPartNode, null);
		final Mesh roofPartMesh = (Mesh) ((Node) roofPartNode).getChild(0);
		computeOrientedBoundingBox(roofPartMesh);
		orgCenters.put((Node) roofPartNode, new Vector3(roofPartMesh.getWorldBound().getCenter()));
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
		for (final ReadOnlyVector3 p : wallUpperPoints)
			maxZ = Math.max(maxZ, p.getZ());
		// to make height relative to container wall so that applyHeight() runs the same way
		height = 5.0 + maxZ - container.getPoints().get(1).getZ();
		// height = height + maxZ - container.getPoints().get(1).getZ(); // avoid this because it will result in increasing height when hovering mouse from one wall contain to another before fully inserting the roof
	}

	public void applyHeight() {
		for (final Vector3 p : points)
			p.setZ(container.getPoints().get(1).getZ() + height);
	}

	protected void postEdit(final EditState editState) {
		draw();
		drawWalls();

		if (editState.fitTestRequired && !windowsFit()) {
			for (int i = 0; i < points.size(); i++)
				points.get(i).set(editState.orgPoints.get(i));
			height = editState.orgHeight;
			draw();
			drawWalls();
		}

		if (container != null)
			setEditPointsVisible(true);
	}

	@Override
	public double computeArea() {
		double area = 0.0;
		for (final Spatial child : roofPartsRoot.getChildren()) {
			final Mesh mesh = (Mesh) ((Node) child).getChild(0);
			final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
			buf.rewind();
			final double annotationScale = Scene.getInstance().getAnnotationScale();
			while (buf.hasRemaining()) {
				final Vector3 p1 = new Vector3(buf.get(), buf.get(), buf.get());
				final Vector3 p2 = new Vector3(buf.get(), buf.get(), buf.get());
				final Vector3 p3 = new Vector3(buf.get(), buf.get(), buf.get());
				final double trigArea = p3.subtract(p1, null).crossLocal(p3.subtract(p2, null)).length() * annotationScale * annotationScale / 2.0;
				area += trigArea;
			}
		}
		return area;
	}

	@Override
	public boolean isDrawable() {
		/* if wallUpperPoints is null then it has not been drawn yet so we assume wallUpperPoints size is okay otherwise all roofs would be invalid at init time */
		return container != null && (wallUpperPoints == null || wallUpperPoints.size() >= 3);
	}
}
