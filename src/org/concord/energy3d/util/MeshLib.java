package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.bounding.OrientedBoundingBox;
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
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		vertexBuffer.rewind();
		normalBuffer.rewind();
		textureBuffer.rewind();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		final Vector3 norm = new Vector3();
		final ArrayList<GroupData> groups = new ArrayList<GroupData>();
		for (int i = 0; i < vertexBuffer.limit() / 9; i++) {
			final Vector3 p1 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p2 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p3 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			if (p1.getZ() == 0 || p2.getZ() == 0 || p3.getZ() == 0)
				continue;
			p2.subtract(p1, v1);
			p3.subtract(p1, v2);
			v1.cross(v2, norm);
			norm.normalizeLocal();

			GroupData group = null;
			for (final GroupData g : groups) {
				if (g.key.dot(norm) > 0.99) { // if there is less than 8 degrees difference between the two vectors
					// if there is an edge in common with the existing triangles
					boolean foundEdgeInCommon = false;
					for (int j = 0; j < g.vertices.size() && !foundEdgeInCommon; j += 3) {
						int numOfShared = 0;
						for (int k = 0; k < 3; k++) {
							final Vector3 p = g.vertices.get(j + k);
							if (p.equals(p1) || p.equals(p2) || p.equals(p3))
								numOfShared++;
						}
						if (numOfShared > 1)
							foundEdgeInCommon = true;
					}
					if (foundEdgeInCommon) {
						group = g;
						break;
					}
				}
			}

			if (group == null) {
				group = new GroupData();
				group.key.set(norm);
				groups.add(group);
			}
			group.vertices.add(p1);
			group.vertices.add(p2);
			group.vertices.add(p3);
			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
			group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
			group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
			group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
		}
		return groups;
	}

	private static void computeHorizontalTextureCoords(final ArrayList<GroupData> groups) {
		for (final GroupData group : groups) {
			final Vector3 normal = group.normals.get(0);

			final Vector3 n1 = new Vector3();
			n1.set(normal.getX(), normal.getY(), 0).normalizeLocal();
			double angleZ = n1.smallestAngleBetween(Vector3.NEG_UNIT_Y);

			if (n1.dot(Vector3.UNIT_X) > 0)
				angleZ = -angleZ;

			final Matrix3 matrixZ = new Matrix3().fromAngles(0, 0, angleZ);

			final Vector3 n2 = new Vector3();
			matrixZ.applyPost(normal, n2);
			final double angleX = n2.smallestAngleBetween(Vector3.NEG_UNIT_Y);

			final Matrix3 matrix = new Matrix3().fromAngles(angleX, 0, 0).multiplyLocal(matrixZ);

			final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 5.0 : 1.0;
			double minV = Double.MAX_VALUE;

			for (int i = 0; i < group.vertices.size(); i++) {
				final Vector3 p = group.vertices.get(i).clone();
				matrix.applyPost(p, p);
				final double v = p.getZ() * scale;
				final double u = p.getX() * scale;
				group.textures.get(i).set(u, v);
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
				newMesh = (Mesh) node.getChild(0);
			} else {
				node = new Node("Roof Part #" + meshIndex);
				newMesh = new Mesh("Roof Mesh #" + meshIndex);
				newMesh.setModelBound(new OrientedBoundingBox());
				node.attachChild(newMesh);
				node.attachChild(new Node("Roof Size Annot"));
				node.attachChild(new Node("Roof Angle Annot"));
				node.attachChild(new BMText("Label Text", "Test", FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center));
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

	public static ArrayList<ReadOnlyVector3> computeConvexHull(final FloatBuffer vertexBuffer) {
		vertexBuffer.rewind();
		final Vector3 leftVertex = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
		while (vertexBuffer.hasRemaining()) {
			final double x = vertexBuffer.get();
			final double y = vertexBuffer.get();
			if (x < leftVertex.getX() || (x == leftVertex.getX() && y < leftVertex.getY()))
				leftVertex.set(x, y, vertexBuffer.get());
			else
				vertexBuffer.position(vertexBuffer.position() + 1);
		}

		final ReadOnlyVector3 normal = Vector3.UNIT_Z;
		final Vector3 pointOnHull = new Vector3(leftVertex);
		final Vector3 endpoint = new Vector3();
		final Vector3 sj = new Vector3();
		final ArrayList<ReadOnlyVector3> convexHull = new ArrayList<ReadOnlyVector3>();
		convexHull.add(new Vector3(pointOnHull));
		do {
			// endpoint.set(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
			endpoint.setX(Double.MAX_VALUE);

			for (int j = 0; j <= vertexBuffer.limit() / 3 - 1; j++) {
				sj.set(vertexBuffer.get(j * 3), vertexBuffer.get(j * 3 + 1), vertexBuffer.get(j * 3 + 2));
				// if (sj.equals(pointOnHull))
				if (convexHull.contains(sj))
					continue;
				// check to see if sj is connected to pointOnHull
				boolean isConnected = false;
				int k = 0;
				while (k < vertexBuffer.limit()) {
					final ReadOnlyVector3 p1 = new Vector3(vertexBuffer.get(k++), vertexBuffer.get(k++), vertexBuffer.get(k++));
					final ReadOnlyVector3 p2 = new Vector3(vertexBuffer.get(k++), vertexBuffer.get(k++), vertexBuffer.get(k++));
					final ReadOnlyVector3 p3 = new Vector3(vertexBuffer.get(k++), vertexBuffer.get(k++), vertexBuffer.get(k++));
					if ((pointOnHull.equals(p1) || pointOnHull.equals(p2) || pointOnHull.equals(p3)) && (sj.equals(p1) || sj.equals(p2) || sj.equals(p3))) {
						isConnected = true;
						break;
					}
				}
				if (!isConnected)
					continue;
				if (endpoint.getX() == Double.MAX_VALUE) {
					if (convexHull.contains(sj))
						continue;
					else
						endpoint.set(sj);
				} else {
					// if (S[j] is on left of line from P[i] to endpoint)
					final double dot = normal.cross(endpoint.subtract(pointOnHull, null).normalizeLocal(), null).dot(sj.subtract(pointOnHull, null).normalizeLocal());
					if (dot > 0) {
						endpoint.set(sj); // found greater left turn, update endpoint
					} else if (dot == 0 && sj.distance(pointOnHull) > endpoint.distance(pointOnHull))
						endpoint.set(sj); // found greater left turn, update endpoint
				}
			}
			pointOnHull.set(endpoint);
			if (endpoint.getX() == Double.MAX_VALUE)
				break;
			else
				convexHull.add(new Vector3(pointOnHull));
		} while (!endpoint.equals(leftVertex));

		final ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();
		for (int i = 1; i < convexHull.size() - 1; i++) {
			final ReadOnlyVector3 p1 = convexHull.get(i - 1);
			final ReadOnlyVector3 p2 = convexHull.get(i);
			final ReadOnlyVector3 p3 = convexHull.get(i + 1);
			if (p2.subtract(p1, null).normalizeLocal().dot(p3.subtract(p1, null).normalizeLocal()) > 1 - MathUtils.ZERO_TOLERANCE)
				toBeRemoved.add(i);
		}
		for (final int i : toBeRemoved)
			convexHull.remove(i);
		return convexHull;
	}

	public static void fillMeshWithPolygon(final Mesh mesh, final Polygon polygon, final XYToAnyTransform fromXY) {
		fillMeshWithPolygon(mesh, polygon, fromXY, 0, null, null, null);
	}

	public static void fillMeshWithPolygon(final Mesh mesh, final Polygon polygon, final XYToAnyTransform fromXY, final double textureScale, final TPoint o, final TPoint u, final TPoint v) {
		// final Vector2 min = new Vector2(Double.MAX_VALUE, Double.MAX_VALUE);
		// final Vector2 max = new Vector2(Double.MIN_VALUE, Double.MIN_VALUE);
		// for (final TriangulationPoint p : polygon.getPoints()) {
		// if (p.getX() > max.getX())
		// max.setX(p.getX());
		// else if (p.getX() < min.getX())
		// min.setX(p.getX());
		//
		// if (p.getY() > max.getY())
		// max.setY(p.getY());
		// else if (p.getY() < min.getY())
		// min.setY(p.getY());
		// }
		//
		// final double c = 0.5;
		// min.addLocal(c, c);
		// max.subtractLocal(c, c);
		//
		// if (polygon.getHoles() != null) {
		// //// polygon.getHoles().remove(3);
		// // polygon.getHoles().remove(9);
		// //// polygon.getHoles().remove(8);
		// // polygon.getHoles().remove(7);
		// // polygon.getHoles().remove(6);
		// // polygon.getHoles().remove(5);
		// // polygon.getHoles().remove(4);
		// //// polygon.getHoles().remove(3);
		// // polygon.getHoles().remove(2);
		// //// polygon.getHoles().remove(1);
		// // polygon.getHoles().remove(0);
		// for (final Polygon hole : polygon.getHoles()) {
		// for (final TriangulationPoint p : hole.getPoints())
		// p.set(MathUtils.clamp(p.getX(), min.getX(), max.getX()), MathUtils.clamp(p.getY(), min.getY(), max.getY()), p.getZ());
		// }
		// }

		// if (polygon.getHoles() != null)
		// for (final Polygon hole : polygon.getHoles()) {
		// for (int i = 0; i < 4; i++) {
		// // for (final TriangulationPoint p : hole.getPoints()) {
		// final TriangulationPoint p = hole.getPoints().get(i);
		// if (!insidePolygon(polygon, p)) {
		// p.set(p.getX() , p.getY() + 0.1, p.getZ());
		// final int ii;
		// if (i == 0)
		// ii = 1;
		// else if (i == 1)
		// ii = 0;
		// else if (i == 2)
		// ii = 3;
		// else
		// ii = 2;
		// final TriangulationPoint p2 = hole.getPoints().get(ii);
		// p2.set(p2.getX(), p2.getY() + 0.1, p2.getZ());
		// // System.err.println("Hole outside wall!");
		// }
		// }
		// }

		try {
			Poly2Tri.triangulate(polygon);
		} catch (final RuntimeException e) {
			// e.printStackTrace();
			// System.out.println("Triangulate exception received with the following polygon:");
			// System.out.println("final Polygon polygon = new Polygon(new PolygonPoint[] {");
			// for (final TriangulationPoint p : polygon.getPoints())
			// System.out.println("\tnew PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "),");
			// System.out.println("});");
			//
			// System.out.println("Polygon hole;");
			// for (final Polygon hole : polygon.getHoles()) {
			// System.out.println("hole = new Polygon(new PolygonPoint[] {");
			// for (final TriangulationPoint p : hole.getPoints())
			// System.out.println("\tnew PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "),");
			// System.out.println("});");
			// System.out.println("polygon.addHole(hole);");
			// }

			throw e;
		}
		ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);
		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles(), fromXY);
		// ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles(), fromXY);
		if (o != null)
			ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), textureScale, o, u, v);
		mesh.getMeshData().updateVertexCount();
		mesh.updateModelBound();
	}

	public static boolean insidePolygon(final Polygon polygon, final TriangulationPoint p) {
		int counter = 0;
		int i;
		double xinters;
		TriangulationPoint p1, p2;

		final int n = polygon.getPoints().size();
		final List<TriangulationPoint> points = polygon.getPoints();
		p1 = points.get(0);
		for (i = 1; i <= n; i++) {
			p2 = points.get(i % n);
			if (p.getY() > Math.min(p1.getY(), p2.getY())) {
				if (p.getY() <= Math.max(p1.getY(), p2.getY())) {
					if (p.getX() <= Math.max(p1.getX(), p2.getX())) {
						if (p1.getY() != p2.getY()) {
							xinters = (p.getY() - p1.getY()) * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY()) + p1.getX();
							if (p1.getX() == p2.getX() || p.getX() <= xinters)
								counter++;
						}
					}
				}
			}
			p1 = p2;
		}

		if (counter % 2 == 0)
			return false;
		else
			return true;
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
		final Vector3 closest = closestPoint(p1, v1, pickRay.getOrigin(), pickRay.getDirection());
		return closest;
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 p21, final ReadOnlyVector3 p3, final ReadOnlyVector3 p43) {
		final double EPS = 0.0001;
		Vector3 p13;
		double d1343, d4321, d1321, d4343, d2121;
		double numer, denom;

		p13 = p1.subtract(p3, null);
		if (Math.abs(p43.getX()) < EPS && Math.abs(p43.getY()) < EPS && Math.abs(p43.getZ()) < EPS)
			return null;
		if (Math.abs(p21.length()) < EPS)
			return null;

		d1343 = p13.getX() * p43.getX() + p13.getY() * p43.getY() + p13.getZ() * p43.getZ();
		d4321 = p43.getX() * p21.getX() + p43.getY() * p21.getY() + p43.getZ() * p21.getZ();
		d1321 = p13.getX() * p21.getX() + p13.getY() * p21.getY() + p13.getZ() * p21.getZ();
		d4343 = p43.getX() * p43.getX() + p43.getY() * p43.getY() + p43.getZ() * p43.getZ();
		d2121 = p21.getX() * p21.getX() + p21.getY() * p21.getY() + p21.getZ() * p21.getZ();

		denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPS)
			return null;
		numer = d1343 * d4321 - d1321 * d4343;

		final double mua = numer / denom;
		final Vector3 pa = new Vector3(p1.getX() + mua * p21.getX(), p1.getY() + mua * p21.getY(), p1.getZ() + mua * p21.getZ());

		return pa;
	}

	public static Vector2 closestPoint(final ReadOnlyVector2 v, final ReadOnlyVector2 w, final ReadOnlyVector2 p) {
		final double l2 = v.distanceSquared(w);
		if (l2 == 0.0)
			return v.clone();
		final double t = p.subtract(v, null).dot(w.subtract(v, null)) / l2; // dot(p - v, w - v) / l2;
		if (t < 0.0)
			return v.clone();
		else if (t > 1.0)
			return w.clone();
		else
			return w.subtract(v, null).multiplyLocal(t).addLocal(v); // v + t * (w - v);
	}

	public static Vector2 snapToPolygon(final ReadOnlyVector3 point, final List<PolygonPoint> polygon) {
		final Vector2 p = new Vector2(point.getX(), point.getY());
		final Vector2 l1 = new Vector2();
		final Vector2 l2 = new Vector2();
		double shortestDistance = Double.MAX_VALUE;
		Vector2 closestPoint = null;
		final int n = polygon.size();
		for (int i = 0; i < n; i++) {
			final PolygonPoint pp1 = polygon.get(i);
			l1.set(pp1.getX(), pp1.getY());
			final PolygonPoint pp2 = polygon.get((i + 1) % n);
			l2.set(pp2.getX(), pp2.getY());
			if (l1.distanceSquared(l2) > MathUtils.ZERO_TOLERANCE) {
				final Vector2 pointOnLine = closestPoint(l1, l2, p);
				final double distance = pointOnLine.distanceSquared(p);
				if (distance < shortestDistance) {
					shortestDistance = distance;
					closestPoint = pointOnLine;
				}
			}
		}
		return closestPoint;
	}
}