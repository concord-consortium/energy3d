package org.concord.energy3d.util;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.geometry.primitives.Point;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.CoordinateTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.LineSegment3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

public class MeshLib {
	private static class GroupData {
		final Vector3 key = new Vector3();
		final ArrayList<ReadOnlyVector3> vertices = new ArrayList<ReadOnlyVector3>();
		final ArrayList<ReadOnlyVector3> normals = new ArrayList<ReadOnlyVector3>();
	}

	public static void groupByPlanner(final Mesh mesh, final Node root) {
		final ArrayList<GroupData> groups = extractGroups(mesh);
		createMeshes(root, groups);
	}

	private static ArrayList<GroupData> extractGroups(final Mesh mesh) {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		final Vector3 normal = new Vector3();
		final ArrayList<GroupData> groups = new ArrayList<GroupData>();
		for (int i = 0; i < vertexBuffer.limit() / 9; i++) {
			final Vector3 p1 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p2 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p3 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			if (p1.getZ() == 0 || p2.getZ() == 0 || p3.getZ() == 0) {
				continue;
			}
			p2.subtract(p1, v1);
			p3.subtract(p1, v2);
			v1.cross(v2, normal);
			normal.normalizeLocal();

			final Vector3 firstNormal = new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get());
			if (Double.isNaN(firstNormal.length())) {
				continue;
			}

			final GroupData group = new GroupData();
			group.key.set(normal);
			groups.add(group);

			group.vertices.add(p1);
			group.vertices.add(p2);
			group.vertices.add(p3);
			group.normals.add(firstNormal);
			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
		}
		combineGroups(groups);
		return groups;
	}

	private static boolean hasCommonEdge(final GroupData group, final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
		boolean foundEdgeInCommon = false;
		for (int j = 0; j < group.vertices.size() && !foundEdgeInCommon; j += 3) {
			int numOfShared = 0;
			for (int k = 0; k < 3; k++) {
				final ReadOnlyVector3 p = group.vertices.get(j + k);
				if (p.equals(p1) || p.equals(p2) || p.equals(p3)) {
					numOfShared++;
				}
			}
			if (numOfShared > 1) {
				foundEdgeInCommon = true;
			}
		}
		return foundEdgeInCommon;
	}

	private static void combineGroups(final ArrayList<GroupData> groups) {
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = 0; i < groups.size(); i++) {
				final GroupData group1 = groups.get(i);
				for (int j = i + 1; j < groups.size(); j++) {
					final GroupData group2 = groups.get(j);
					if (isSameDirection(group1.key, group2.key)) {
						for (int w = 0; w < group2.vertices.size(); w += 3) {
							final ReadOnlyVector3 p1 = group2.vertices.get(w);
							final ReadOnlyVector3 p2 = group2.vertices.get(w + 1);
							final ReadOnlyVector3 p3 = group2.vertices.get(w + 2);
							if (hasCommonEdge(group1, p1, p2, p3)) {
								group1.vertices.addAll(group2.vertices);
								group1.normals.addAll(group2.normals);
								groups.remove(group2);
								j--;
								changed = true;
								break;
							}
						}
					}
				}
			}
		}
	}

	public static boolean isSameDirection(final ReadOnlyVector3 a, final ReadOnlyVector3 b) {
		return a.dot(b) > 0.99;
	}

	public static void createMeshes(final Node root, final ArrayList<GroupData> groups) {
		if (groups.size() != root.getNumberOfChildren()) {
			root.detachAllChildren();
		}

		int meshIndex = 0;
		for (final GroupData group : groups) {
			final Node node;
			final Mesh mesh;
			final Mesh meshWithHoles;
			final BMText label;
			if (meshIndex < root.getNumberOfChildren()) {
				node = (Node) root.getChild(meshIndex);
				mesh = (Mesh) node.getChild(0);
				label = (BMText) node.getChild(3);
				meshWithHoles = (Mesh) node.getChild(6);
				node.getSceneHints().setAllPickingHints(true);
			} else {
				node = new Node("Roof Part #" + meshIndex);
				mesh = new Mesh("Roof Mesh #" + meshIndex);
				meshWithHoles = new Mesh("Roof Mesh with Holes #" + meshIndex);
				mesh.setVisible(false);
				mesh.setModelBound(new BoundingBox());
				meshWithHoles.setModelBound(new BoundingBox());
				meshWithHoles.setRenderState(HousePart.offsetState);

				label = new BMText("Label Text", "", FontManager.getInstance().getPartNumberFont(), Align.South, Justify.Center);
				Util.initHousePartLabel(label);

				final Mesh wireframeMesh = new Line("Roof (wireframe)");
				wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
				wireframeMesh.setModelBound(new BoundingBox());
				wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(10));
				// offset to avoid z-fighting
				wireframeMesh.setTranslation(group.key.multiply(0.001, null));

				final Line dashLineMesh = new Line("Roof (dash line)");
				dashLineMesh.setStipplePattern((short) 0xFF00);
				dashLineMesh.setVisible(false);
				dashLineMesh.setModelBound(new BoundingBox());

				final Node sizeAnnotation = new Node("Roof Size Annot");
				final Node angleAnnotation = new Node("Roof Angle Annot");

				// disable picking for all except mesh
				Util.disablePickShadowLight(sizeAnnotation);
				Util.disablePickShadowLight(angleAnnotation);
				Util.disablePickShadowLight(wireframeMesh);
				Util.disablePickShadowLight(dashLineMesh);
				// meshWithHoles.getSceneHints().setAllPickingHints(false);

				node.attachChild(mesh);
				node.attachChild(sizeAnnotation);
				node.attachChild(angleAnnotation);
				node.attachChild(label);
				node.attachChild(wireframeMesh);
				node.attachChild(dashLineMesh);
				node.attachChild(meshWithHoles);

				root.attachChild(node);
			}

			node.getSceneHints().setCullHint(CullHint.Never);
			CollisionTreeManager.getInstance().removeCollisionTree(mesh);
			CollisionTreeManager.getInstance().removeCollisionTree(meshWithHoles);

			final Vector3 normal = new Vector3();
			for (final ReadOnlyVector3 v : group.normals) {
				normal.addLocal(v);
			}
			normal.normalizeLocal();
			node.setUserData(normal);

			final FloatBuffer buf = BufferUtils.createVector3Buffer(group.vertices.size());
			mesh.getMeshData().setVertexBuffer(buf);
			final Vector3 center = new Vector3();
			for (final ReadOnlyVector3 v : group.vertices) {
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
				center.addLocal(v);
			}
			center.multiplyLocal(1.0 / group.vertices.size());
			label.setTranslation(center.add(normal.multiply(0.1, null), null));

			mesh.updateModelBound();
			meshIndex++;
		}
	}

	public static void applyHoles(final Node root, final List<Window> windows) {
		final Map<Window, List<ReadOnlyVector3>> holes = new HashMap<Window, List<ReadOnlyVector3>>();
		for (final Window window : windows) {
			final ArrayList<ReadOnlyVector3> hole = new ArrayList<ReadOnlyVector3>();
			hole.add(window.getAbsPoint(0).multiplyLocal(1, 1, 0));
			hole.add(window.getAbsPoint(2).multiplyLocal(1, 1, 0));
			hole.add(window.getAbsPoint(3).multiplyLocal(1, 1, 0));
			hole.add(window.getAbsPoint(1).multiplyLocal(1, 1, 0));
			holes.put(window, hole);
		}

		for (int roofIndex = 0; roofIndex < root.getChildren().size(); roofIndex++) {
			final Spatial roofPart = root.getChildren().get(roofIndex);
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final ReadOnlyVector3 normal = (ReadOnlyVector3) roofPart.getUserData();
				final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
				final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

				final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
				final ArrayList<ReadOnlyVector3> points3D = computeOutline(mesh.getMeshData().getVertexBuffer());
				final List<PolygonPoint> points2D = new ArrayList<PolygonPoint>();

				final ReadOnlyVector3 firstPoint = points3D.get(0);
				final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 0.5 : 0.1;
				final TPoint o;
				final TPoint u;
				final TPoint v;
				if (normal.dot(Vector3.UNIT_Z) == 1) {
					o = new TPoint(firstPoint.getX(), firstPoint.getY(), firstPoint.getZ());
					u = new TPoint(1 / scale, 0, 0);
					v = new TPoint(0, 1 / scale, 0);
				} else {
					final ReadOnlyVector3 u3 = Vector3.UNIT_Z.cross(normal, null).normalizeLocal();
					final ReadOnlyVector3 ou3 = u3.divide(scale, null).add(firstPoint, null);
					final ReadOnlyVector3 ov3 = normal.cross(u3, null).divideLocal(scale).addLocal(firstPoint);
					o = new TPoint(firstPoint.getX(), firstPoint.getY(), firstPoint.getZ());
					u = new TPoint(ou3.getX(), ou3.getY(), ou3.getZ());
					v = new TPoint(ov3.getX(), ov3.getY(), ov3.getZ());

					toXY.transform(o);
					toXY.transform(u);
					toXY.transform(v);

					u.set(u.getX() - o.getX(), u.getY() - o.getY(), 0);
					v.set(v.getX() - o.getX(), v.getY() - o.getY(), 0);
				}

				final Vector2 o2 = new Vector2(firstPoint.getX(), firstPoint.getY());
				final Vector2 ou2 = o2.add(new Vector2(u.getX(), u.getY()), null);
				final Vector2 ov2 = o2.add(new Vector2(v.getX(), v.getY()), null);
				double minLineScaleU = Double.MAX_VALUE;
				double minLineScaleV = Double.MAX_VALUE;
				for (final ReadOnlyVector3 p : points3D) {
					final PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
					toXY.transform(polygonPoint);
					points2D.add(polygonPoint);
					final Vector2 p2 = new Vector2(polygonPoint.getX(), polygonPoint.getY());
					final double lineScaleU = Util.projectPointOnLineScale(p2, o2, ou2);
					final double lineScaleV = Util.projectPointOnLineScale(p2, o2, ov2);

					if (lineScaleU < minLineScaleU) {
						minLineScaleU = lineScaleU;
					}
					if (lineScaleV < minLineScaleV) {
						minLineScaleV = lineScaleV;
					}
				}
				o2.addLocal(new Vector2(u.getX(), u.getY()).multiplyLocal(minLineScaleU));
				o2.addLocal(new Vector2(v.getX(), v.getY()).multiplyLocal(minLineScaleV));
				final PolygonWithHoles polygon = new PolygonWithHoles(points2D);
				o.set(o2.getX(), o2.getY(), 0);

				roofPart.updateWorldBound(true);
				for (final Window window : windows) {
					if (holes.get(window) == null) {
						continue;
					}
					final List<PolygonPoint> holePolygon = new ArrayList<PolygonPoint>();
					boolean outside = false;
					for (final ReadOnlyVector3 holePoint : holes.get(window)) {
						final PickResults pickResults = new PrimitivePickResults();
						PickingUtil.findPick(((Node) roofPart).getChild(0), new Ray3(holePoint, Vector3.UNIT_Z), pickResults, false);
						if (pickResults.getNumber() > 0) {
							final ReadOnlyVector3 intersectionPoint = pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0);
							final PolygonPoint polygonPoint = new PolygonPoint(intersectionPoint.getX(), intersectionPoint.getY(), intersectionPoint.getZ());
							toXY.transform(polygonPoint);
							holePolygon.add(polygonPoint);
						} else {
							outside = true;
							break;
						}
					}
					if (!outside) {
						polygon.addHole(new PolygonWithHoles(holePolygon));
						holes.remove(window);
						window.setRoofIndex(roofIndex);
					}
				}

				final Mesh meshWithHoles = (Mesh) ((Node) roofPart).getChild(6);
				try {
					fillMeshWithPolygon(meshWithHoles, polygon, fromXY, true, o, v, u, false);
				} catch (final RuntimeException e) {
					e.printStackTrace();
					final Mesh meshWithoutHoles = (Mesh) ((Node) roofPart).getChild(0);
					meshWithHoles.setMeshData(meshWithoutHoles.getMeshData());
				}
			}
		}
	}

	public static ArrayList<ReadOnlyVector3> computeOutline(final FloatBuffer buf) {
		final Map<LineSegment3, Boolean> visitMap = new HashMap<LineSegment3, Boolean>();
		for (int i = 0; i < buf.limit(); i += 9) {
			for (int trianglePointIndex = 0; trianglePointIndex < 9; trianglePointIndex += 3) {
				buf.position(i + trianglePointIndex);
				Vector3 p1 = new Vector3(buf.get(), buf.get(), buf.get());
				buf.position(i + (trianglePointIndex + 3) % 9);
				Vector3 p2 = new Vector3(buf.get(), buf.get(), buf.get());
				if (p2.getX() < p1.getX() || (p2.getX() == p1.getX() && p2.getY() < p1.getY())) {
					final Vector3 tmp = p1;
					p1 = p2;
					p2 = tmp;
				}
				final LineSegment3 line = new LineSegment3(p1, p2);
				final Boolean pastVisit = visitMap.get(line);
				if (pastVisit == null) {
					visitMap.put(line, true);
				} else {
					visitMap.put(line, false);
				}
			}
		}

		final ArrayList<ReadOnlyVector3> outlinePoints = new ArrayList<ReadOnlyVector3>();
		for (final LineSegment3 line : visitMap.keySet()) {
			if (visitMap.get(line)) {
				final Vector3 negativeEnd = line.getNegativeEnd(null);
				outlinePoints.add(negativeEnd);
				outlinePoints.add(line.getPositiveEnd(null));
			}
		}

		final ArrayList<ReadOnlyVector3> sortedOutlinePoints = new ArrayList<ReadOnlyVector3>(outlinePoints.size() / 2);
		sortedOutlinePoints.add(outlinePoints.get(0));
		ReadOnlyVector3 lastPoint = outlinePoints.get(1);
		sortedOutlinePoints.add(lastPoint);
		outlinePoints.remove(1);
		outlinePoints.remove(0);
		while (!outlinePoints.isEmpty()) {
			boolean foundSomething = false;
			for (int i = 0; i < outlinePoints.size(); i++) {
				if (Util.isEqual(outlinePoints.get(i), lastPoint)) {
					final int otherEndIndex = i % 2 == 0 ? i + 1 : i - 1;
					lastPoint = outlinePoints.get(otherEndIndex);
					sortedOutlinePoints.add(lastPoint);
					outlinePoints.remove(Math.max(i, otherEndIndex));
					outlinePoints.remove(Math.min(i, otherEndIndex));
					foundSomething = true;
					break;
				}
			}
			if (!foundSomething) {
				break;
			}
		}

		// remove last point if duplicated of first point
		if (Util.isEqual(sortedOutlinePoints.get(0), sortedOutlinePoints.get(sortedOutlinePoints.size() - 1))) {
			sortedOutlinePoints.remove(sortedOutlinePoints.size() - 1);
		}

		// for (int i = 0; i < sortedOutlinePoints.size(); i++) {
		// final ReadOnlyVector3 p1 = sortedOutlinePoints.get(i);
		// final ReadOnlyVector3 p2 = sortedOutlinePoints.get((i + 1) % sortedOutlinePoints.size());
		// final ReadOnlyVector3 p3 = sortedOutlinePoints.get((i + 2) % sortedOutlinePoints.size());
		//
		// if (isAlmost180(p1, p2, p3)) {
		// sortedOutlinePoints.remove((i + 1) % sortedOutlinePoints.size());
		// i--;
		// }
		// }

		return sortedOutlinePoints;
	}

	// private static boolean isAlmost180(final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
	// return Math.abs(p1.subtract(p2, null).normalizeLocal().smallestAngleBetween(p3.subtract(p1, null).normalizeLocal())) > Math.PI - Math.PI / 180.0;
	// }

	public static void fillMeshWithPolygon(final Mesh mesh, final PolygonWithHoles polygon, final CoordinateTransform fromXY, final boolean generateNormals, final TPoint o, final TPoint u, final TPoint v, final boolean isWall) {
		/* round all points */
		for (final Point p : polygon.getPoints()) {
			p.set(Util.round(p.getX()), Util.round(p.getY()), Util.round(p.getZ()));
		}
		if (polygon.getHoles() != null) {
			for (final Polygon hole : polygon.getHoles()) {
				for (final Point p : hole.getPoints()) {
					p.set(Util.round(p.getX()), Util.round(p.getY()), Util.round(p.getZ()));
				}
			}
		}

		/* remove holes that collide with polygon or other holes */
		if (polygon.getHoles() != null) {
			// ensure polygon doesn't collide with holes
			final Path2D polygonPath = Util.makePath2D(polygon.getPoints());
			final Map<Polygon, Object> skipHoles = new HashMap<Polygon, Object>();
			for (final Polygon hole : polygon.getHoles()) {
				for (final Point p : hole.getPoints()) {
					if (!polygonPath.contains(new Point2D.Double(p.getX(), p.getY()))) {
						skipHoles.put(hole, null);
						break;
					}
				}
			}
			// ensure holes don't collide with each other
			for (int i = 0; i < polygon.getHoles().size(); i++) {
				final Polygon hole1 = polygon.getHoles().get(i);
				if (skipHoles.containsKey(hole1)) {
					continue;
				}
				for (int j = i + 1; j < polygon.getHoles().size(); j++) {
					final Polygon hole2 = polygon.getHoles().get(j);
					if (skipHoles.containsKey(hole2)) {
						continue;
					}
					boolean found = false;
					for (final Point p : hole2.getPoints()) {
						if (Util.insidePolygon(p, hole1.getPoints())) {
							skipHoles.put(hole2, null);
							found = true;
							break;
						}
					}
					if (!found) {
						final int n1 = hole1.getPoints().size();
						for (int i1 = 0; i1 < n1; i1++) {
							final Point l1p1 = hole1.getPoints().get(i1);
							final Point l1p2 = hole1.getPoints().get((i1 + 1) % n1);
							final Line2D line1 = new Line2D.Double(l1p1.getX(), l1p1.getY(), l1p2.getX(), l1p2.getY());
							found = false;
							final int n2 = hole2.getPoints().size();
							for (int i2 = 0; i2 < n2; i2++) {
								final Point l2p1 = hole2.getPoints().get(i2);
								final Point l2p2 = hole2.getPoints().get((i2 + 1) % n2);
								final Line2D line2 = new Line2D.Double(l2p1.getX(), l2p1.getY(), l2p2.getX(), l2p2.getY());
								if (line2.intersectsLine(line1)) {
									skipHoles.put(hole2, null);
									found = true;
									break;
								}
							}
							if (found) {
								break;
							}
						}
					}
				}
			}
			for (final Polygon hole : skipHoles.keySet()) {
				polygon.getHoles().remove(hole);
			}
		}

		try {
			Poly2Tri.triangulate(polygon);
			if (fromXY == null) {
				ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
			} else {
				ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);
			}

			if (generateNormals) {
				if (fromXY == null) {
					ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
				} else {
					ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles(), fromXY);
				}
			}
			if (o != null) {
				ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1.0, o, u, v);
			}
			mesh.getMeshData().updateVertexCount();
			mesh.updateModelBound();
		} catch (final RuntimeException e) {
			System.err.println("Points:");
			for (final Point p : polygon.getPoints()) {
				System.err.println(p);
			}
			System.err.println("Holes:");
			if (polygon.getHoles() != null) {
				for (final Polygon hole : polygon.getHoles()) {
					for (final Point p : hole.getPoints()) {
						System.err.println(p);
					}
				}
			}
			throw e;
		}
	}

}