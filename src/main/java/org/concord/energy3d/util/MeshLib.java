package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.primitives.Point;
import org.poly2tri.transform.coordinate.CoordinateTransform;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.LineSegment3;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

public class MeshLib {
	private static class GroupData {
		final Vector3 key = new Vector3();
		final ArrayList<Vector3> vertices = new ArrayList<Vector3>();
		final ArrayList<Vector3> normals = new ArrayList<Vector3>();
		final ArrayList<Vector2> textures = new ArrayList<Vector2>();
	}

	public static void groupByPlanner(final Mesh mesh, final Node root) {
		final ArrayList<GroupData> groups = extractGroups(mesh);
		computeHorizontalTextureCoords(groups);
		createMeshes(root, groups);
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

			GroupData group = null;
			for (final GroupData g : groups) {
				if (g.key.dot(normal) > 0.99) { // if there is less than 8 degrees difference between the two vectors
					// if there is an edge in common with the existing triangles
					if (hasCommonEdge(g, p1, p2, p3)) {
						group = g;
						break;
					}
				}
			}

			if (group == null) {
				group = new GroupData();
				group.key.set(normal);
				groups.add(group);
			}

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

	private static boolean hasCommonEdge(final GroupData group, final Vector3 p1, final Vector3 p2, final Vector3 p3) {
		boolean foundEdgeInCommon = false;
		for (int j = 0; j < group.vertices.size() && !foundEdgeInCommon; j += 3) {
			int numOfShared = 0;
			for (int k = 0; k < 3; k++) {
				final Vector3 p = group.vertices.get(j + k);
				if (p.equals(p1) || p.equals(p2) || p.equals(p3))
					numOfShared++;
			}
			if (numOfShared > 1)
				foundEdgeInCommon = true;
		}
		return foundEdgeInCommon;
	}

	private static void combineGroups(final ArrayList<GroupData> groups) {
		for (int i = 0; i < groups.size(); i++) {
			final GroupData group1 = groups.get(i);
			for (int j = i + 1; j < groups.size(); j++) {
				final GroupData group2 = groups.get(j);
				boolean changed = false;
				if (group1.key.equals(group2.key)) {
					for (int w = 0; w < group2.vertices.size() / 3; w++) {
						final Vector3 p1 = group2.vertices.get(w * 3);
						final Vector3 p2 = group2.vertices.get(w * 3 + 1);
						final Vector3 p3 = group2.vertices.get(w * 3 + 2);
						if (hasCommonEdge(group1, p1, p2, p3)) {
							group1.vertices.add(p1);
							group1.vertices.add(p2);
							group1.vertices.add(p3);
							group1.normals.add(group2.normals.get(w * 3));
							group1.normals.add(group2.normals.get(w * 3 + 1));
							group1.normals.add(group2.normals.get(w * 3 + 2));
							for (int count = 0; count < 3; count++) {
								group2.vertices.remove(w);
								group2.normals.remove(w);
							}
							changed = true;
							if (group2.vertices.isEmpty()) {
								groups.remove(group2);
								break;
							} else
								w = 0;
						}
					}
				}
				if (changed)
					j = i + 1;
			}
		}
	}

	private static void computeHorizontalTextureCoords(final ArrayList<GroupData> groups) {
		for (final GroupData group : groups) {
			final Vector3 normal = group.normals.get(0);

			final Vector3 n1 = new Vector3(normal.getX(), normal.getY(), 0).normalizeLocal();
			double angleZ = n1.smallestAngleBetween(Vector3.NEG_UNIT_Y);

			if (n1.dot(Vector3.UNIT_X) > 0)
				angleZ = -angleZ;

			final Matrix3 matrixZ = new Matrix3().fromAngles(0, 0, angleZ);

			final Vector3 n2 = new Vector3();
			matrixZ.applyPost(normal, n2);
			final double angleX = n2.smallestAngleBetween(Vector3.NEG_UNIT_Y);

			final Matrix3 matrix = new Matrix3().fromAngles(angleX, 0, 0).multiplyLocal(matrixZ);

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

	public static void createMeshes(final Node root, final ArrayList<GroupData> groups) {
		int meshIndex = 0;
		for (final GroupData group : groups) {
			final Node node;
			final Mesh newMesh;
			if (meshIndex < root.getNumberOfChildren()) {
				node = (Node) root.getChild(meshIndex);
				node.setName("Roof Part #" + meshIndex);
				newMesh = (Mesh) node.getChild(0);
			} else {
				node = new Node("Roof Part #" + meshIndex);
				newMesh = new Mesh("Roof Mesh #" + meshIndex);
				newMesh.setRenderState(HousePart.offsetState);
				newMesh.setModelBound(new OrientedBoundingBox());
				node.attachChild(newMesh);
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
			}
			final Vector3 normal = new Vector3();
			for (final Vector3 v : group.normals)
				normal.addLocal(v);
			normal.normalizeLocal();
			node.setUserData(normal);

			FloatBuffer buf = newMesh.getMeshData().getVertexBuffer();
			int n = group.vertices.size();
			buf = BufferUtils.createVector3Buffer(n);
			newMesh.getMeshData().setVertexBuffer(buf);
			final Vector3 center = new Vector3();
			for (final Vector3 v : group.vertices) {
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
				center.addLocal(v);
			}
			center.multiplyLocal(1.0 / group.vertices.size());

			buf = newMesh.getMeshData().getNormalBuffer();
			n = group.normals.size();
			buf = BufferUtils.createVector3Buffer(n);
			newMesh.getMeshData().setNormalBuffer(buf);
			for (final Vector3 v : group.normals)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());

			buf = newMesh.getMeshData().getTextureBuffer(0);
			n = group.textures.size();
			buf = BufferUtils.createVector2Buffer(n);
			newMesh.getMeshData().setTextureBuffer(buf, 0);
			for (final Vector2 v : group.textures)
				buf.put(v.getXf()).put(v.getYf());

			newMesh.getMeshData().updateVertexCount();
			CollisionTreeManager.INSTANCE.updateCollisionTree(newMesh);
			newMesh.updateModelBound();
			node.getChild(3).setTranslation(center.add(normal.multiply(0.1, null), null));

			meshIndex++;
		}

		while (meshIndex < root.getNumberOfChildren()) {
			root.detachChildAt(root.getNumberOfChildren() - 1);
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
					endpoint.set(sj); // found greater left turn, update endpoint
				else if (!sj.equals(pointOnHull) && dot == 0 && sj.distance(pointOnHull) > endpoint.distance(pointOnHull))
					endpoint.set(sj); // found greater left turn, update endpoint
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
				if (outlinePoints.get(i).distanceSquared(lastPoint) < MathUtils.ZERO_TOLERANCE) {
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
		if (sortedOutlinePoints.get(0).distanceSquared(sortedOutlinePoints.get(sortedOutlinePoints.size() - 1)) < MathUtils.ZERO_TOLERANCE)
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

	public static void fillMeshWithPolygon(final Mesh mesh, final Polygon polygon, final CoordinateTransform fromXY, final boolean generateNormals, final TPoint o, final TPoint u, final TPoint v) {
		/* round all points */
		for (final Point p : polygon.getPoints())
			p.set(Util.round(p.getX()), Util.round(p.getY()), Util.round(p.getZ()));
		if (polygon.getHoles() != null)
			for (final Polygon hole : polygon.getHoles())
				for (final Point p : hole.getPoints())
					p.set(Util.round(p.getX()), Util.round(p.getY()), Util.round(p.getZ()));

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