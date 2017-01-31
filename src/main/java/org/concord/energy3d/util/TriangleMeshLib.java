package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.MeshLib.GroupData;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class TriangleMeshLib {

	public static List<Mesh> getPlanarMeshes(final Mesh mesh) {
		return createMeshes(findGroups(mesh));
	}

	private static ArrayList<GroupData> findGroups(final Mesh mesh) {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		if (normalBuffer == null) {
			normalBuffer = BufferUtils.createFloatBuffer(vertexBuffer.limit());
		} else {
			normalBuffer.rewind();
		}
		final TextureState textureState = (TextureState) mesh.getLocalRenderState(StateType.Texture);
		FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		if (textureBuffer != null) {
			textureBuffer.rewind();
		} else {
			textureBuffer = BufferUtils.createFloatBuffer(vertexBuffer.limit() / 3 * 2);
		}
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		final Vector3 normal = new Vector3();
		final ArrayList<GroupData> groups = new ArrayList<GroupData>();
		for (int i = 0; i < vertexBuffer.limit() / 9; i++) {
			final Vector3 p1 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p2 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 p3 = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
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
			if (textureState != null && textureState.getTexture() != null) {
				group.textureImage = textureState.getTexture().getImage();
			}

			group.vertices.add(p1);
			group.vertices.add(p2);
			group.vertices.add(p3);
			group.normals.add(firstNormal);
			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
			group.normals.add(new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get()));
			if (textureBuffer != null) {
				group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get())); // texture is 2D, vertex is 3D
				group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
				group.textures.add(new Vector2(textureBuffer.get(), textureBuffer.get()));
			}
		}
		MeshLib.combineGroups(groups);
		return groups;
	}

	private static List<Mesh> createMeshes(final ArrayList<GroupData> groups) {
		final List<Mesh> results = new ArrayList<Mesh>();
		int meshIndex = 0;
		for (final GroupData group : groups) {
			final Mesh mesh = new Mesh("Mesh #" + meshIndex++);
			mesh.setModelBound(new BoundingBox());
			results.add(mesh);

			final Vector3 normal = new Vector3();
			for (final ReadOnlyVector3 v : group.normals) {
				normal.addLocal(v);
			}
			normal.normalizeLocal();
			mesh.setUserData(normal);

			final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(group.vertices.size());
			mesh.getMeshData().setVertexBuffer(vertexBuffer);
			final Vector3 center = new Vector3();
			for (final ReadOnlyVector3 v : group.vertices) {
				vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
				center.addLocal(v);
			}
			center.multiplyLocal(1.0 / group.vertices.size());

			if (!group.textures.isEmpty()) {
				final FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(group.textures.size());
				mesh.getMeshData().setTextureBuffer(textureBuffer, 0);
				for (final ReadOnlyVector2 v : group.textures) {
					textureBuffer.put(v.getXf()).put(v.getYf());
				}
				if (group.textureImage != null) {
					final Texture texture = TextureManager.loadFromImage(group.textureImage, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat);
					final TextureState ts = new TextureState();
					ts.setTexture(texture);
					mesh.setRenderState(ts);
				}
			}
			mesh.updateModelBound();
		}
		return results;
	}

	// remove this in the future
	static Node convertNode(final Foundation foundation, final Node oldNode) {
		final Node newNode = new Node();
		final List<Mesh> oldMeshes = new ArrayList<Mesh>();
		Util.getMeshes(oldNode, oldMeshes);
		final double scale = Scene.getInstance().getAnnotationScale() * 0.633; // 0.633 is determined by fitting the length in Energy3D to the length in SketchUp
		for (final Mesh m : oldMeshes) {
			m.setUserData(new UserData(foundation)); // an imported mesh doesn't necessarily have the same normal vector (e.g., a cube could be a whole mesh in collada)
			final MeshData md = m.getMeshData();
			switch (md.getIndexMode(0)) {
			case Triangles:
				final FloatBuffer vertexBuffer = md.getVertexBuffer();
				final FloatBuffer normalBuffer = md.getNormalBuffer();
				final FloatBuffer colorBuffer = md.getColorBuffer();
				final int n = (int) Math.round(vertexBuffer.limit() / 9.0);
				for (int i = 0; i < n; i++) {
					final Mesh mesh = new Mesh("Triangle");
					final FloatBuffer vb = BufferUtils.createFloatBuffer(9);
					final int j = i * 9;
					vb.put(vertexBuffer.get(j)).put(vertexBuffer.get(j + 1)).put(vertexBuffer.get(j + 2));
					vb.put(vertexBuffer.get(j + 3)).put(vertexBuffer.get(j + 4)).put(vertexBuffer.get(j + 5));
					vb.put(vertexBuffer.get(j + 6)).put(vertexBuffer.get(j + 7)).put(vertexBuffer.get(j + 8));
					mesh.getMeshData().setVertexBuffer(vb);
					final UserData userData = new UserData(foundation);
					if (normalBuffer != null) {
						final FloatBuffer nb = BufferUtils.createFloatBuffer(9);
						nb.put(normalBuffer.get(j)).put(normalBuffer.get(j + 1)).put(normalBuffer.get(j + 2));
						nb.put(normalBuffer.get(j + 3)).put(normalBuffer.get(j + 4)).put(normalBuffer.get(j + 5));
						nb.put(normalBuffer.get(j + 6)).put(normalBuffer.get(j + 7)).put(normalBuffer.get(j + 8));
						mesh.getMeshData().setNormalBuffer(nb);
						userData.setNormal(new Vector3(nb.get(0), nb.get(1), nb.get(2)));
					}
					mesh.setUserData(userData);
					if (colorBuffer != null) {
						final FloatBuffer cb = BufferUtils.createFloatBuffer(9);
						cb.put(colorBuffer.get(j)).put(colorBuffer.get(j + 1)).put(colorBuffer.get(j + 2));
						cb.put(colorBuffer.get(j + 3)).put(colorBuffer.get(j + 4)).put(colorBuffer.get(j + 5));
						cb.put(colorBuffer.get(j + 6)).put(colorBuffer.get(j + 7)).put(colorBuffer.get(j + 8));
						mesh.getMeshData().setColorBuffer(cb);
					}
					mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(3), 0);
					mesh.setRenderState(HousePart.offsetState);
					mesh.getMeshData().setIndexMode(md.getIndexMode(0));
					mesh.setScale(scale);
					newNode.attachChild(mesh);
				}
				break;
			case Lines:
				break;
			default:
				System.out.println("*******" + md.getIndexMode(0));
				break;
			}
		}
		return newNode;
	}

}