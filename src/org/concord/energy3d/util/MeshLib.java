package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.geom.BufferUtils;

public class MeshLib {
	
	public static void groupByPlanner(final Mesh mesh, final Node root) {
		class GroupData {
			final Vector3 key = new Vector3();
			final ArrayList<Vector3> vertices = new ArrayList<Vector3>();
			final ArrayList<Vector3> normals = new ArrayList<Vector3>();
			final ArrayList<Vector2> textures = new ArrayList<Vector2>();
		}
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		vertexBuffer.rewind();
		normalBuffer.rewind();
		textureBuffer.rewind();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		final Vector3 norm = new Vector3();
//		final Map<Vector3, GroupData> groups = new HashMap<Vector3, GroupData>();
		final ArrayList<GroupData> groups = new ArrayList<GroupData>();
		for (int i = 0; i < vertexBuffer.limit() / 9; i++) {
			final Vector3 p1 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p2 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p3 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			p2.subtract(p1, v1);
			p3.subtract(p1, v2);
			v1.cross(v2, norm);
			norm.normalizeLocal();
//			System.out.print(norm + "\t");
//			norm.set(round(norm.getX()), round(norm.getY()), round(norm.getZ()));
//			System.out.println(norm);
			
			GroupData group = null;
			for (final GroupData g : groups) {
//				if (Math.round(g.key.dot(norm) * 1000000) / 1000000 == 1 ||  g.key.smallestAngleBetween(norm) < 0.05) {
				if (g.key.dot(norm) > 0.999) {	// if there is less than 2 degrees difference between the two vectors
					System.out.println(g.key.smallestAngleBetween(norm));
					group = g;
					break;
				}
			}			
			
//			GroupData group = groups.get(norm);
			if (group == null) {
				group = new GroupData();
				group.key.set(norm);
//				System.out.println(norm);
//				groups.put(new Vector3(norm), group);
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
		
		int meshIndex = 0;
//		for (GroupData group : groups.values()) {
		for (final GroupData group : groups) {
			final Mesh newMesh;
			if (meshIndex < root.getNumberOfChildren()) {
				newMesh = (Mesh)root.getChild(meshIndex);
				newMesh.getSceneHints().setCullHint(CullHint.Inherit);
			} else {
				newMesh = new Mesh("Roof Part #" + meshIndex);
				root.attachChild(newMesh);
			}
			
			FloatBuffer buf = newMesh.getMeshData().getVertexBuffer();
			int n = group.vertices.size();
			if (buf == null || buf.capacity() / 3 < n) {
				buf = BufferUtils.createVector3Buffer(n);
				newMesh.getMeshData().setVertexBuffer(buf);
			}
			buf.rewind();
			buf.limit(n * 3);
			for (final Vector3 v : group.vertices)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
			
			buf = newMesh.getMeshData().getNormalBuffer();
			n = group.normals.size();
			if (buf == null || buf.capacity() / 3 < n) {
				buf = BufferUtils.createVector3Buffer(n);
				newMesh.getMeshData().setNormalBuffer(buf);
			}
			buf.rewind();
			buf.limit(n * 3);			
			for (Vector3 v : group.normals)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
			
			buf = newMesh.getMeshData().getTextureBuffer(0);
			n = group.textures.size();
			if (buf == null || buf.capacity() / 2 < n) {
				buf = BufferUtils.createVector2Buffer(n);
				newMesh.getMeshData().setTextureBuffer(buf, 0);
			}				
			buf.rewind();
			buf.limit(n * 2);			
			for (Vector2 v : group.textures)
				buf.put(v.getXf()).put(v.getYf());

			newMesh.getMeshData().updateVertexCount();
			newMesh.updateModelBound();
			newMesh.updateWorldBound(false);
			meshIndex++;
		}		
		while(meshIndex < root.getNumberOfChildren()) {
			root.getChild(meshIndex).getSceneHints().setCullHint(CullHint.Always);
			meshIndex++;
		}
	}	

//	private static double round(double x) {
//		return Math.round(x * 10) / 10.0;
//	}

}