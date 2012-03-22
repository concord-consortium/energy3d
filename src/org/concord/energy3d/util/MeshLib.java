package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
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
			p2.subtract(p1, v1);
			p3.subtract(p1, v2);
			v1.cross(v2, norm);
			norm.normalizeLocal();

			GroupData group = null;
			for (final GroupData g : groups) {
				if (g.key.dot(norm) > 0.99) { // if there is less than 8 degrees difference between the two vectors
					for (final Vector3 groupPoint : g.vertices)
						if (groupPoint.equals(p1) || groupPoint.equals(p2) || groupPoint.equals(p3)) {
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

			double minV = Double.MAX_VALUE;

			for (int i = 0; i < group.vertices.size(); i++) {
				final Vector3 p = group.vertices.get(i).clone();
				matrix.applyPost(p, p);
				final double v = p.getZ();
				final double u = p.getX();
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
				node.getSceneHints().setCullHint(CullHint.Inherit);
				node.getSceneHints().setPickingHint(PickingHint.Pickable, true);
			} else {
				newMesh = new Mesh("Roof Mesh #" + meshIndex);
				newMesh.setModelBound(new OrientedBoundingBox());
//				final OffsetState offsetState = new OffsetState();
//				offsetState.setTypeEnabled(OffsetType.Fill, true);
//				offsetState.setFactor(1);
//				offsetState.setUnits(1);
//				newMesh.setRenderState(offsetState);
				node = new Node("Roof Part #" + meshIndex);
				node.attachChild(newMesh);
				node.attachChild(new Node("Roof Size Annot"));
				node.attachChild(new Node("Roof Angle Annot"));
				node.attachChild(new BMText("Label Text", "Test", FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center));
				node.getChild(3).getSceneHints().setCullHint(CullHint.Always);

				final Mesh wireframeMesh = new Line("Roof (wireframe)");
//				((Line)wireframeMesh).setLineWidth(5);
				wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
				wireframeMesh.setModelBound(new BoundingBox());
				wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(10));
				Util.disablePickShadowLight(wireframeMesh);
				node.attachChild(wireframeMesh);

//				if (root.getNumberOfChildren() != 0)
//					newMesh.getSceneHints().setCullHint(CullHint.Always);
				root.attachChild(node);
			}
			final Vector3 normal = new Vector3();
			for (final Vector3 v : group.normals)
				normal.addLocal(v);
			normal.normalizeLocal();
			node.setUserData(normal);

//			final Vector3 normal = new Vector3();
//			for (final Vector3 v : group.normals)
//				normal.addLocal(v);
//			normal.normalizeLocal();
//			if (!Vector3.isValid(normal))
//				continue;

			FloatBuffer buf = newMesh.getMeshData().getVertexBuffer();
			int n = group.vertices.size();
			if (buf == null || buf.capacity() / 3 < n) {
				buf = BufferUtils.createVector3Buffer(n);
				newMesh.getMeshData().setVertexBuffer(buf);
			}
			buf.rewind();
			buf.limit(n * 3);
			final Vector3 center = new Vector3();
			for (final Vector3 v : group.vertices) {
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
				center.addLocal(v);
			}
			center.multiplyLocal(1.0 / group.vertices.size());

			buf = newMesh.getMeshData().getNormalBuffer();
			n = group.normals.size();
			if (buf == null || buf.capacity() / 3 < n) {
				buf = BufferUtils.createVector3Buffer(n);
				newMesh.getMeshData().setNormalBuffer(buf);
			}
			buf.rewind();
			buf.limit(n * 3);
			for (final Vector3 v : group.normals)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());

			buf = newMesh.getMeshData().getTextureBuffer(0);
			n = group.textures.size();
			if (buf == null || buf.capacity() / 2 < n) {
				buf = BufferUtils.createVector2Buffer(n);
				newMesh.getMeshData().setTextureBuffer(buf, 0);
			}
			buf.rewind();
			buf.limit(n * 2);
			for (final Vector2 v : group.textures)
				buf.put(v.getXf()).put(v.getYf());

			newMesh.getMeshData().updateVertexCount();
			newMesh.updateModelBound();
			node.getChild(3).setTranslation(center.add(normal.multiply(0.1, null), null));

			meshIndex++;
//			break;
		}
		while (meshIndex < root.getNumberOfChildren()) {
			root.getChild(meshIndex).getSceneHints().setCullHint(CullHint.Always);
			root.getChild(meshIndex).getSceneHints().setPickingHint(PickingHint.Pickable, false);
			meshIndex++;
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
			endpoint.set(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
			for (int j = 1; j <= vertexBuffer.limit() / 3 - 1; j++) {
				sj.set(vertexBuffer.get(j * 3), vertexBuffer.get(j * 3 + 1), vertexBuffer.get(j * 3 + 2));
				// if (S[j] is on left of line from P[i] to endpoint)
				final double dot = normal.cross(endpoint.subtract(pointOnHull, null).normalizeLocal(), null).dot(sj.subtract(pointOnHull, null).normalizeLocal());
				if (!sj.equals(pointOnHull) && dot > 0) {
					endpoint.set(sj); // found greater left turn, update endpoint
				} else if (!sj.equals(pointOnHull) && dot == 0 && sj.distance(pointOnHull) > endpoint.distance(pointOnHull))
					endpoint.set(sj); // found greater left turn, update endpoint
			}
			pointOnHull.set(endpoint);
			convexHull.add(new Vector3(pointOnHull));
		} while (!endpoint.equals(leftVertex));
		convexHull.contains(endpoint);
		return convexHull;
	}
}