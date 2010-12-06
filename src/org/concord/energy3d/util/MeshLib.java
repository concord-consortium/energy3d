package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.geom.BufferUtils;

public class MeshLib {
	
	public static void groupByPlanner(final Mesh mesh, final Node root) {
		class GroupData {
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
		final Map<Vector3, GroupData> groups = new HashMap<Vector3, GroupData>();
		for (int i = 0; i < vertexBuffer.limit() / 9; i++) {
//			System.out.println(vertexBuffer.capacity() + "\t" + i);
			final Vector3 p1 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p2 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p3 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			p2.subtract(p1, v1);
			p3.subtract(p1, v2);
			v1.cross(v2, norm).normalizeLocal();
			norm.set(round(norm.getX()), round(norm.getY()), round(norm.getZ()));
			GroupData group = groups.get(norm);
			if (group == null) {
				group = new GroupData();
				groups.put(new Vector3(norm), group);
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
		for (GroupData group : groups.values()) {
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
			System.out.println(newMesh + "\t primitives = " + newMesh.getMeshData().getPrimitiveCount(0));
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
//		for (RenderState rs : mesh.getLocalRenderStates().values())
//			root.setRenderState(rs);
//		return root;
	}	

//	public static Node groupByPlanner(final Mesh mesh, final Node flattenedMeshesRoot) {
//		class GroupData {
//			final ArrayList<Vector3> vertices = new ArrayList<Vector3>();
//			final ArrayList<Vector3> normals = new ArrayList<Vector3>();
//			final ArrayList<Vector2> textures = new ArrayList<Vector2>();
//		}
//		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
//		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
//		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
//		vertexBuffer.rewind();
//		normalBuffer.rewind();
//		textureBuffer.rewind();
//		final Vector3 v1 = new Vector3();
//		final Vector3 v2 = new Vector3();
//		final Vector3 n = new Vector3();
//		final Map<Vector3, GroupData> groups = new HashMap<Vector3, GroupData>();
//		for (int i = 0; i < vertexBuffer.capacity() / 9; i++) {
//			final Vector3 p1 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
//			final Vector3 p2 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
//			final Vector3 p3 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
//			p2.subtract(p1, v1);
//			p3.subtract(p1, v2);
//			v1.cross(v2, n).normalizeLocal();
//			n.set(round(n.getX()), round(n.getY()), round(n.getZ()));
//			GroupData group = groups.get(n);
//			if (group == null) {
//				group = new GroupData();
//				groups.put(new Vector3(n), group);
//			}
//			group.vertices.add(p1);
//			group.vertices.add(p2);
//			group.vertices.add(p3);
//			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
//			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
//			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
//			group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
//			group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
//			group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
//		}
////		final Node root = new Node("Grouped by Normal Root "); 
//		
//		for (GroupData group : groups.values()) {
////			final Mesh newMesh = new Mesh();
//			
//			FloatBuffer buf = BufferUtils.createVector3Buffer(group.vertices.size());
//			for (Vector3 v : group.vertices)
//				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
//			newMesh.getMeshData().setVertexBuffer(buf);
//			
//			buf = BufferUtils.createVector3Buffer(group.normals.size());
//			for (Vector3 v : group.normals)
//				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
//			buf = BufferUtils.createVector3Buffer(group.textures.size());
//			
//			newMesh.getMeshData().setNormalBuffer(buf);
//			for (Vector2 v : group.textures)
//				buf.put(v.getXf()).put(v.getYf());
//			newMesh.getMeshData().setTextureBuffer(buf, 0);
//
//			newMesh.updateModelBound();
//			newMesh.updateWorldBound(false);
//			root.attachChild(newMesh);			
//		}
//		for (RenderState rs : mesh.getLocalRenderStates().values())
//			root.setRenderState(rs);
////		return root;
//	}

	private static double round(double x) {
		return Math.round(x * 10) / 10.0;
	}

}
