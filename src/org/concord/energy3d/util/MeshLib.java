package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
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
				if (g.key.dot(norm) > 0.999) {	// if there is less than 2 degrees difference between the two vectors
					group = g;
					break;
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
	
	private static void computeHorizontalTextureCoords(ArrayList<GroupData> groups) {
		for (final GroupData group : groups) {
//			int baseIndex_1 = 0;
//			int baseIndex_2 = 1;
//			for (int i = 2; i < group.vertices.size(); i++) {
//				final double z = group.vertices.get(i).getZ();
//				if (z < group.vertices.get(baseIndex_1).getZ())
//					baseIndex_1 = i;
//				else if (z < group.vertices.get(baseIndex_2).getZ())
//					baseIndex_2 = i;
//			}
//			final Vector3 normal = new Vector3(group.normals.get(0));
//			normal.setZ(0);
//			final double angle = normal.smallestAngleBetween(Vector3.UNIT_Y.negate(null));
////			final Vector3 rotAxis = normal.cross(Vector3.UNIT_Y, null);
//			final Matrix3 matrix = new Matrix3().fromAngles(0, 0, angle); //.fromAngleAxis(angle, rotAxis);
////			final Vector3 base = new Vector3(group.vertices.get(baseIndex_2)).subtractLocal(group.vertices.get(baseIndex_1)).normalizeLocal();
////			final double angle_2 = base.smallestAngleBetween(Vector3.UNIT_X);
////			matrix.multiplyLocal(new Matrix3().fromAngles(0, angle_2, 0));
//			System.out.println("angle = " + (int)(angle / Math.PI * 180));
//			matrix.fromAngles(0, 0, angle);
//			base.su

			final Vector3 normal = group.normals.get(0);
			final Vector3 n1 = new Vector3();
			n1.set(normal.getX(), normal.getY(), 0).normalizeLocal();
//			normal.setZ(0);
//			normal.normalizeLocal();
//			final double angle = normal.smallestAngleBetween(Vector3.UNIT_Y.negate(null));
			final double angle = -n1.smallestAngleBetween(Vector3.UNIT_Y);
			
			
			final Matrix3 matrix = new Matrix3().fromAngles(0, 0, angle);
			
			final Vector3 n2 = new Vector3();
			matrix.applyPost(normal, n2);
			System.out.println("n2 = " + n2);
			final double angleX = n2.smallestAngleBetween(Vector3.UNIT_Y.negate(null));
			System.out.println("angleX = " + (int)(angleX / Math.PI * 180));
			matrix.multiplyLocal(new Matrix3().fromAngles(-angleX, 0, 0));
			
			final Vector3 min = new Vector3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			final Vector3 max = new Vector3(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
			for (final Vector3 p : group.vertices) {
				min.set(Math.min(min.getX(), p.getX()), Math.min(min.getY(), p.getY()), Math.min(min.getZ(), p.getZ()));
				max.set(Math.max(max.getX(), p.getX()), Math.max(max.getY(), p.getY()), Math.max(max.getZ(), p.getZ()));
			}
			final double maxV = max.getZ() - min.getZ();
			min.setZ(0);
			max.setZ(0);
			final double maxU = min.distance(max);
			System.out.println("---");
			for (int i = 0; i < group.vertices.size(); i++) {
				final Vector3 p = group.vertices.get(i).clone();
//				System.out.println(p);
				matrix.applyPost(p, p);
//				System.out.println(p);
//				p.subtractLocal(min);
				final double v = p.getZ();
				System.out.println("z = " + p.getZ());
//				p.setZ(0);
				final double u = p.getX();
				group.textures.get(i).set(u, v);
				System.out.println(u + ", " + v + "\t\t\t" + p);
			}
		}
	}

	public static void createMeshes(final Node root, final ArrayList<GroupData> groups) {
		int meshIndex = 0;
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

	
}