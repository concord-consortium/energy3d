package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.geometry.primitives.Point;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.CoordinateTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.LineSegment3;
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
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

public class MeshLib {
	private static class GroupData {
		final Vector3 key = new Vector3();
		final ArrayList<ReadOnlyVector3> vertices = new ArrayList<ReadOnlyVector3>();
		final ArrayList<ReadOnlyVector3> normals = new ArrayList<ReadOnlyVector3>();
		final ArrayList<Vector2> textures = new ArrayList<Vector2>();
	}

	public static void groupByPlanner(final Mesh mesh, final Node root, final List<List<ReadOnlyVector3>> holes) {
		final ArrayList<GroupData> groups = extractGroups(mesh);
		computeHorizontalTextureCoords(groups);
		createMeshes(root, groups);
		applyHoles(root, holes);
	}

	public static ArrayList<GroupData> extractGroups(final Mesh mesh) {
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
			if (p1.getZ() == 0 || p2.getZ() == 0 || p3.getZ() == 0)
				continue;
			p2.subtract(p1, v1);
			p3.subtract(p1, v2);
			v1.cross(v2, normal);
			normal.normalizeLocal();

			final Vector3 firstNormal = new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get());
			if (Double.isNaN(firstNormal.length()))
				continue;

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
				if (p.equals(p1) || p.equals(p2) || p.equals(p3))
					numOfShared++;
			}
			if (numOfShared > 1)
				foundEdgeInCommon = true;
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
					if (group1.key.dot(group2.key) > 0.99) {
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

	private static void computeHorizontalTextureCoords(final ArrayList<GroupData> groups) {
		for (final GroupData group : groups) {
			final ReadOnlyVector3 normal = group.normals.get(0);

			final Matrix3 matrix = toXYMatrix(normal);

			final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 0.5 : 0.1;
			double minV = Double.MAX_VALUE;
			for (int i = 0; i < group.vertices.size(); i++) {
				final Vector3 p = group.vertices.get(i).clone();
				matrix.applyPost(p, p);
				final double v = p.getZ() * scale;
				final double u = p.getX() * scale;
				group.textures.add(new Vector2(u, v));
				if (minV > v)
					minV = v;
			}

			for (final Vector2 t : group.textures)
				t.addLocal(0, -minV);
		}
	}

	private static Matrix3 toXYMatrix(final ReadOnlyVector3 normal) {
		final Vector3 n1 = new Vector3(normal.getX(), normal.getY(), 0).normalizeLocal();
		double angleZ = n1.smallestAngleBetween(Vector3.NEG_UNIT_Y);

		if (n1.dot(Vector3.UNIT_X) > 0)
			angleZ = -angleZ;

		final Matrix3 matrixZ = new Matrix3().fromAngles(0, 0, angleZ);

		final Vector3 n2 = new Vector3();
		matrixZ.applyPost(normal, n2);
		final double angleX = n2.smallestAngleBetween(Vector3.NEG_UNIT_Y);

		final Matrix3 matrix = new Matrix3().fromAngles(angleX, 0, 0).multiplyLocal(matrixZ);
		return matrix;
	}

	public static void createMeshes(final Node root, final ArrayList<GroupData> groups) {
		root.detachAllChildren();
		int meshIndex = 0;
		for (final GroupData group : groups) {
			final Node node = new Node("Roof Part #" + meshIndex);
			final Mesh mesh = new Mesh("Roof Mesh #" + meshIndex);
			mesh.setRenderState(HousePart.offsetState);
			mesh.setModelBound(new BoundingBox());

			node.attachChild(mesh);
			node.attachChild(new Node("Roof Size Annot"));
			node.attachChild(new Node("Roof Angle Annot"));
			final BMText label = new BMText("Label Text", "Test", FontManager.getInstance().getPartNumberFont(), Align.South, Justify.Center);
			Util.initHousePartLabel(label);
			label.setTranslation(group.key);
			node.attachChild(label);
			node.getChild(3).getSceneHints().setCullHint(CullHint.Always);

			final Mesh wireframeMesh = new Line("Roof (wireframe)");
			wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
			wireframeMesh.setModelBound(new BoundingBox());
			wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(10));
			// offset to avoid z-fighting
			wireframeMesh.setTranslation(group.key.multiply(0.001, null));
			Util.disablePickShadowLight(wireframeMesh);
			node.attachChild(wireframeMesh);

			final Line dashLineMesh = new Line("Roof (dash line)");
			dashLineMesh.setStipplePattern((short) 0xFF00);
			dashLineMesh.setVisible(false);
			dashLineMesh.setModelBound(new BoundingBox());
			Util.disablePickShadowLight(dashLineMesh);
			node.attachChild(dashLineMesh);
			root.attachChild(node);

			final Vector3 normal = new Vector3();
			for (final ReadOnlyVector3 v : group.normals)
				normal.addLocal(v);
			normal.normalizeLocal();
			node.setUserData(normal);

			FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
			int n = group.vertices.size();
			buf = BufferUtils.createVector3Buffer(n);
			mesh.getMeshData().setVertexBuffer(buf);
			final Vector3 center = new Vector3();
			for (final ReadOnlyVector3 v : group.vertices) {
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
				center.addLocal(v);
			}
			center.multiplyLocal(1.0 / group.vertices.size());

			buf = mesh.getMeshData().getNormalBuffer();
			n = group.normals.size();
			buf = BufferUtils.createVector3Buffer(n);
			mesh.getMeshData().setNormalBuffer(buf);
			for (final ReadOnlyVector3 v : group.normals)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());

			buf = mesh.getMeshData().getTextureBuffer(0);
			n = group.textures.size();
			buf = BufferUtils.createVector2Buffer(n);
			mesh.getMeshData().setTextureBuffer(buf, 0);
			for (final Vector2 v : group.textures)
				buf.put(v.getXf()).put(v.getYf());

			mesh.getMeshData().updateVertexCount();
			CollisionTreeManager.INSTANCE.updateCollisionTree(mesh);
			mesh.updateModelBound();
			node.getChild(3).setTranslation(center.add(normal.multiply(0.1, null), null));

			meshIndex++;
		}
	}
	
	private static void applyHoles(Node root, List<List<ReadOnlyVector3>> holes) {		
		for (final Spatial roofPart : root.getChildren()) {
//			Spatial roofPart = root.getChild(3);
			final ReadOnlyVector3 normal = (ReadOnlyVector3) roofPart.getUserData();
			final Matrix3 matrix = toXYMatrix(normal);
			final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
			final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());
			
			final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
			final ArrayList<ReadOnlyVector3> points3D = computeOutline(mesh.getMeshData().getVertexBuffer());			
			final List<PolygonPoint> points2D = new ArrayList<PolygonPoint>();
			
			
			final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 0.5 : 0.1;
			TPoint o = new TPoint(points3D.get(0).getX(), points3D.get(0).getY(), points3D.get(0).getZ());
			final ReadOnlyVector3 pU = normal.cross(Vector3.UNIT_Z, null).normalizeLocal();
			final ReadOnlyVector3 pOU = pU.divide(scale, null).add(points3D.get(0), null);
			final TPoint u = new TPoint(pOU.getX(), pOU.getY(), pOU.getZ());
			final ReadOnlyVector3 pOV = normal.cross(pU, null).divide(scale, null).addLocal(points3D.get(0));
			final TPoint v = new TPoint(pOV.getX(), pOV.getY(), pOV.getZ());
			
			toXY.transform(o);
			toXY.transform(u);
			toXY.transform(v);
			
			u.set(u.getX() - o.getX(), u.getY() - o.getY(), 0);
			v.set(v.getX() - o.getX(), v.getY() - o.getY(), 0);
			
			
			
			final Vector3 p2D = new Vector3();
			final Vector3 topLeftPoint = new Vector3(Double.MAX_VALUE, 0, Double.MAX_VALUE);
//			ReadOnlyVector3 highestPoint = null;
			for (final ReadOnlyVector3 p : points3D) {
//				matrix.applyPost(p, p2D);
//				final PolygonPoint xyPoint = new PolygonPoint(p2D.getX(), p2D.getY(), p2D.getZ());
				final PolygonPoint xyPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(xyPoint);
				points2D.add(xyPoint);
				
//				if (highestPoint == null || p.getZ() > highestPoint.getZ())
//					highestPoint = p;
				
				if (xyPoint.getY() > topLeftPoint.getY()) {
					topLeftPoint.setY(xyPoint.getY());
				}				
				if (xyPoint.getX() < topLeftPoint.getX())
					topLeftPoint.setX(xyPoint.getX());				
			}
			final PolygonWithHoles polygon = new PolygonWithHoles(points2D);
			o = new TPoint(topLeftPoint.getX(), topLeftPoint.getY(), 0);
			
//			roofPart.updateWorldBound(true);
//			for (final List<ReadOnlyVector3> hole : holes) {
//				final List<PolygonPoint> holePolygon = new ArrayList<PolygonPoint>();
//				boolean outside = false;
//				for (final ReadOnlyVector3 holePoint : hole) {
//					final PickResults pickResults = new PrimitivePickResults();
//					PickingUtil.findPick(roofPart, new Ray3(holePoint, Vector3.UNIT_Z), pickResults, false);
//					if (pickResults.getNumber() > 0) {
//						final ReadOnlyVector3 intersectionPoint = pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0);
//						matrix.applyPost(intersectionPoint, p2D);
//						final PolygonPoint polygonPoint = new PolygonPoint(p2D.getX(), p2D.getY(), p2D.getZ());
////						toXY.transform(polygonPoint);
//						holePolygon.add(polygonPoint);
//					} else {
//						outside = true;
//						break;
//					}
////					holePolygon.add(new PolygonPoint(-10, 5, 39.474810643661));
////					holePolygon.add(new PolygonPoint(10, 5, 39.474810643661));
////					holePolygon.add(new PolygonPoint(10, 10, 39.474810643661));
////					holePolygon.add(new PolygonPoint(-10, 10, 39.474810643661));
//				}
//				if (!outside) {
////					holePolygon.add(holePolygon.get(0));
////					polygon.addHole(new PolygonWithHoles(holePolygon));
//				}
//			}
			
			fillMeshWithPolygon(mesh, polygon, null, true, o, v, u);
//			fillMeshWithPolygon(mesh, polygon, fromXY, true, null, null, null);
//			fillMeshWithPolygon(mesh, polygon, null, true, null, null, null);

//			// Compute texture coordinates			
//			final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 0.5 : 0.1;
//			double minU = Double.MAX_VALUE;
//			double minV = Double.MAX_VALUE;
//			final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
//			final FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(mesh.getMeshData().getVertexCount());
//			mesh.getMeshData().setTextureBuffer(textureBuffer, 0);
//			final Vector3 p = new Vector3();
//			final Vector2 uv = new Vector2();
//			for (int i = 0; i < mesh.getMeshData().getVertexCount(); i++) {
////				vertexBuffer.position(i * 3);
//				textureBuffer.position(i * 2);
//				BufferUtils.populateFromBuffer(p, vertexBuffer, i);
////				p.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
////				matrix.applyPost(p, p);
//				uv.set(p.getX() * scale, p.getZ() * scale);
////				final float u = (float) (p.getX() * scale);
////				final float v = (float) (p.getZ() * scale);
////				textureBuffer.put(u);
////				textureBuffer.put(v);
//				BufferUtils.addInBuffer(uv, textureBuffer, i);
//				if (minU > uv.getX())
//					minU = uv.getX();
//				if (minV > uv.getY())
//					minV = uv.getY();
////				matrix.invertLocal().applyPre(p, p);
//				BufferUtils.addInBuffer(p, vertexBuffer, i);
//			}
//
//			for (int i = 0; i < mesh.getMeshData().getVertexCount(); i++) {
//				BufferUtils.populateFromBuffer(uv, textureBuffer, i);
////				final int index = i * 2;
////				float u = textureBuffer.get(index);
////				textureBuffer.put(index, u - minU);				
////				float v = textureBuffer.get(index + 1);
////				textureBuffer.put(index + 1, v - minV);
//				uv.subtractLocal(minU, minV);
//				BufferUtils.addInBuffer(uv, textureBuffer, i);
//			}			
		}
	}		

	public static void addConvexWireframe(final FloatBuffer wireframeVertexBuffer, final FloatBuffer vertexBuffer) {
		vertexBuffer.rewind();
		final Vector3 leftVertex = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
		while (vertexBuffer.hasRemaining()) {
			final double x = vertexBuffer.get();
			if (x < leftVertex.getX())
				leftVertex.set(x, vertexBuffer.get(), vertexBuffer.get());
			else
				vertexBuffer.position(vertexBuffer.position() + 2);
		}

		final ReadOnlyVector3 normal = Vector3.UNIT_Z;
		final Vector3 pointOnHull = new Vector3(leftVertex);
		final Vector3 endpoint = new Vector3();
		final Vector3 sj = new Vector3();
		do {
			wireframeVertexBuffer.put(pointOnHull.getXf()).put(pointOnHull.getYf()).put(pointOnHull.getZf());
			endpoint.set(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
			for (int j = 1; j <= vertexBuffer.limit() / 3 - 1; j++) {
				sj.set(vertexBuffer.get(j * 3), vertexBuffer.get(j * 3 + 1), vertexBuffer.get(j * 3 + 2));
				// if (S[j] is on left of line from P[i] to endpoint)
				final double dot = normal.cross(endpoint.subtract(pointOnHull, null), null).dot(sj.subtract(pointOnHull, null));
				if (!sj.equals(pointOnHull) && dot > 0)
					endpoint.set(sj); // found greater left turn, update
										// endpoint
				else if (!sj.equals(pointOnHull) && dot == 0 && sj.distance(pointOnHull) > endpoint.distance(pointOnHull))
					endpoint.set(sj); // found greater left turn, update
										// endpoint
			}
			pointOnHull.set(endpoint);
			wireframeVertexBuffer.put(pointOnHull.getXf()).put(pointOnHull.getYf()).put(pointOnHull.getZf());
		} while (!endpoint.equals(leftVertex));
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
				if (pastVisit == null)
					visitMap.put(line, true);
				else
					visitMap.put(line, false);
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
			if (!foundSomething)
				break;
		}

		// remove last point if duplicated of first point
		if (Util.isEqual(sortedOutlinePoints.get(0), sortedOutlinePoints.get(sortedOutlinePoints.size() - 1)))
			sortedOutlinePoints.remove(sortedOutlinePoints.size() - 1);

		for (int i = 0; i < sortedOutlinePoints.size(); i++) {
			final ReadOnlyVector3 p1 = sortedOutlinePoints.get(i);
			final ReadOnlyVector3 p2 = sortedOutlinePoints.get((i + 1) % sortedOutlinePoints.size());
			final ReadOnlyVector3 p3 = sortedOutlinePoints.get((i + 2) % sortedOutlinePoints.size());

			if (isAlmost180(p1, p2, p3)) {
				sortedOutlinePoints.remove((i + 1) % sortedOutlinePoints.size());
				i--;
			}
		}

		return sortedOutlinePoints;
	}

	private static boolean isAlmost180(final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
		return Math.abs(p1.subtract(p2, null).normalizeLocal().smallestAngleBetween(p3.subtract(p1, null).normalizeLocal())) > Math.PI - Math.PI / 180.0;
	}

	public static void fillMeshWithPolygon(final Mesh mesh, final PolygonWithHoles polygon, final CoordinateTransform fromXY, final boolean generateNormals, final TPoint o, final TPoint u, final TPoint v) {
		/* round all points */
		for (final Point p : polygon.getPoints())
			p.set(Util.round(p.getX()), Util.round(p.getY()), Util.round(p.getZ()));
		if (polygon.getHoles() != null)
			for (final Polygon hole : polygon.getHoles())
				for (final Point p : hole.getPoints())
					p.set(Util.round(p.getX()), Util.round(p.getY()), Util.round(p.getZ()));

		/* remove holes that collide with polygon or other holes */
		if (polygon.getHoles() != null) {
			final TriangulationPoint tp1 = polygon.getPoints().get(0);
			final TriangulationPoint tp0 = polygon.getPoints().get(1);
			final TriangulationPoint tp2 = polygon.getPoints().get(2);
			final ReadOnlyVector2 p0 = new Vector2(tp0.getX(), tp0.getY());
			final ReadOnlyVector2 p1 = new Vector2(tp1.getX(), tp1.getY());
			final ReadOnlyVector2 p2 = new Vector2(tp2.getX(), tp2.getY());

			double minX, minY, maxX, maxY;
			minX = minY = Double.POSITIVE_INFINITY;
			maxX = maxY = Double.NEGATIVE_INFINITY;
			for (final Point tp : polygon.getPoints()) {
				final ReadOnlyVector2 p = new Vector2(tp.getX(), tp.getY());
				final double pX = Util.projectPointOnLine(p, p0, p2, false).distance(p0);
				final double pY = Util.projectPointOnLine(p, p0, p1, false).distance(p0);
				if (pX < minX)
					minX = pX;
				if (pX > maxX)
					maxX = pX;
				if (pY < minY)
					minY = pY;
				if (pY > maxY)
					maxY = pY;
			}
			for (int i = 0; i < polygon.getHoles().size(); i++) {
				final Polygon hole1 = polygon.getHoles().get(i);
				double minX1, minY1, maxX1, maxY1;
				minX1 = minY1 = Double.POSITIVE_INFINITY;
				maxX1 = maxY1 = Double.NEGATIVE_INFINITY;
				for (final Point tp : hole1.getPoints()) {
					/*
					 * ensure p is within the rectangular boundaries of the polygon
					 */
					final Vector2 p = new Vector2(tp.getX(), tp.getY());
					final double sX = Util.projectPointOnLineScale(p, p0, p2);
					final double sY = Util.projectPointOnLineScale(p, p0, p1);
					final double pX = Util.projectPointOnLine(p, p0, p2, false).distance(p0) * Math.signum(sX);
					final double pY = Util.projectPointOnLine(p, p0, p1, false).distance(p0) * Math.signum(sY);
					final ReadOnlyVector2 xDir = p2.subtract(p0, null).normalizeLocal();
					final ReadOnlyVector2 yDir = p1.subtract(p0, null).normalizeLocal();

					if (pX <= minX)
						p.addLocal(xDir.multiply(minX - pX + 0.1, null));
					else if (pX >= maxX)
						p.addLocal(xDir.multiply(maxX - pX - 0.1, null));
					if (pY <= minY)
						p.addLocal(yDir.multiply(minY - pY + 0.1, null));
					else if (pY >= maxY)
						p.addLocal(yDir.multiply(maxY - pY - 0.1, null));

					tp.set(p.getX(), p.getY(), tp.getZ());

					if (pX < minX1)
						minX1 = pX;
					if (pX > maxX1)
						maxX1 = pX;
					if (pY < minY1)
						minY1 = pY;
					if (pY > maxY1)
						maxY1 = pY;
				}
				for (int j = polygon.getHoles().size() - 1; j > i; j--) {
					final Polygon hole2 = polygon.getHoles().get(j);
					double minX2, minY2, maxX2, maxY2;
					minX2 = minY2 = Double.POSITIVE_INFINITY;
					maxX2 = maxY2 = Double.NEGATIVE_INFINITY;
					for (final Point tp : hole2.getPoints()) {
						final ReadOnlyVector2 p = new Vector2(tp.getX(), tp.getY());
						final double pX = Util.projectPointOnLine(p, p0, p2, false).distance(p0);
						final double pY = Util.projectPointOnLine(p, p0, p1, false).distance(p0);
						if (pX < minX2)
							minX2 = pX;
						if (pX > maxX2)
							maxX2 = pX;
						if (pY < minY2)
							minY2 = pY;
						if (pY > maxY2)
							maxY2 = pY;
					}
					final boolean isOutside = (minX2 < minX1 && maxX2 < minX1 || minX2 > maxX1 && maxX2 > maxX1) || (minY2 < minY1 && maxY2 < minY1 || minY2 > maxY1 && maxY2 > maxY1);
					if (!isOutside)
						polygon.getHoles().remove(hole2);
				}
			}
		}

		Poly2Tri.triangulate(polygon);
		if (fromXY == null)
			ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
		else
			ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);

		if (generateNormals) {
			if (fromXY == null)
				ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
			else
				ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles(), fromXY);
		}
		if (o != null)
			ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1.0, o, u, v);
		mesh.getMeshData().updateVertexCount();
		mesh.updateModelBound();
	}
}