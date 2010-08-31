package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.geom.BufferUtils;

public class MeshLib {

	public static Node groupByPlanner(final Mesh mesh) {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		final Vector3 n = new Vector3();
		final Map<Vector3, ArrayList<Vector3>> groups = new HashMap<Vector3, ArrayList<Vector3>>();
		for (int i = 0; i < vertexBuffer.capacity() / 9; i++) {
			final Vector3 p1 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p2 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p3 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			p2.subtract(p1, v1);
			p3.subtract(p1, v2);
			v1.cross(v2, n).normalizeLocal();
			n.set(round(n.getX()), round(n.getY()), round(n.getZ()));
			ArrayList<Vector3> meshVertices = groups.get(n);
			if (meshVertices == null) {
				meshVertices = new ArrayList<Vector3>();
				groups.put(new Vector3(n), meshVertices);
			}
			meshVertices.add(p1);
			meshVertices.add(p2);
			meshVertices.add(p3);							
		}
		final Node root = new Node("Grouped by Normal Root "); 
		for (ArrayList<Vector3> group : groups.values()) {
			final Mesh newMesh = new Mesh();
			newMesh.setDefaultColor(ColorRGBA.RED);
			final FloatBuffer buf = BufferUtils.createVector3Buffer(group.size());
			buf.rewind();
			for (Vector3 v : group)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
			newMesh.getMeshData().setVertexBuffer(buf);
			root.attachChild(newMesh);
		}
		return root;
	}

	private static double round(double x) {
		return Math.round(x * 10) / 10.0;
	}

}
