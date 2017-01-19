package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.util.MeshLib.GroupData;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.geom.BufferUtils;

public class MeshLib2 {

	public static void groupByPlanner(final Mesh mesh, final Node root, final boolean checkZeroZ) {
		final ArrayList<GroupData> groups = extractGroups(mesh, checkZeroZ);
		createMeshes(root, groups);
	}

	private static ArrayList<GroupData> extractGroups(final Mesh mesh, final boolean checkZeroZ) {
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
			if (checkZeroZ && (p1.getZ() == 0 || p2.getZ() == 0 || p3.getZ() == 0)) {
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
					if (MeshLib.isSameDirection(group1.key, group2.key)) {
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

	public static void createMeshes(final Node root, final ArrayList<GroupData> groups) {
		if (groups.size() != root.getNumberOfChildren()) {
			root.detachAllChildren();
		}

		int meshIndex = 0;
		for (final GroupData group : groups) {
			final Mesh mesh = new Mesh("Mesh #" + meshIndex);
			mesh.setModelBound(new BoundingBox());
			root.attachChild(mesh);

			final Vector3 normal = new Vector3();
			for (final ReadOnlyVector3 v : group.normals) {
				normal.addLocal(v);
			}
			normal.normalizeLocal();
			mesh.setUserData(normal);

			final FloatBuffer buf = BufferUtils.createVector3Buffer(group.vertices.size());
			mesh.getMeshData().setVertexBuffer(buf);
			mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(6), 0);
			final Vector3 center = new Vector3();
			for (final ReadOnlyVector3 v : group.vertices) {
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
				center.addLocal(v);
			}
			center.multiplyLocal(1.0 / group.vertices.size());

			mesh.updateModelBound();
			meshIndex++;
		}
	}

}