package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

class NodeWorker {

	static void reach(final Node node) {
		final List<Spatial> collidables = new ArrayList<Spatial>();
		collidables.addAll(node.getChildren());
		collidables.add(SceneManager.getInstance().getLand());
		int count = 0;
		for (final Spatial s : node.getChildren()) {
			reach((Mesh) s, collidables);
			if (count % 20 == 0) {
				EnergyPanel.getInstance().progress((int) Math.round(100.0 * count / node.getNumberOfChildren()));
			}
			count++;
		}
		EnergyPanel.getInstance().progress(0);
	}

	// If a ray in the direction of the normal of this mesh doesn't hit anything, it is considered as an exterior face of a twin mesh. Otherwise, it is considered as the interior face.
	private static void reach(final Mesh mesh, final List<Spatial> collidables) {

		final UserData userData = (UserData) mesh.getUserData();
		final ReadOnlyVector3 normal = userData.getRotatedNormal() == null ? userData.getNormal() : userData.getRotatedNormal();

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		final List<ReadOnlyVector3> vertices = new ArrayList<ReadOnlyVector3>(vertexBuffer.limit() / 3);
		while (vertexBuffer.hasRemaining()) {
			vertices.add(mesh.localToWorld(new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()), null));
		}
		final Vector3 p = new Vector3();

		// use only the center
		// for (final ReadOnlyVector3 v : vertices) {
		// p.addLocal(v);
		// }
		// p.multiplyLocal(1.0 / vertices.size());

		// check if the centers of the triangles can be reached, if one can, then the entire mesh is considered as an exterior face
		boolean reachable = false;
		final PickResults pickResults = new PrimitivePickResults();
		final int n = vertices.size() / 3; // assuming triangular meshes
		for (int i = 0; i < n; i++) {

			// get the center of the triangle
			p.zero();
			p.addLocal(vertices.get(3 * i));
			p.addLocal(vertices.get(3 * i + 1));
			p.addLocal(vertices.get(3 * i + 2));
			p.multiplyLocal(1.0 / 3.0);

			// we must apply the offset transfer as these points come from the vertex buffer that is not affected by the translation definition of the mesh
			// p.addLocal(normal.multiply(node.getMeshThickness(), null));

			// detect collision
			pickResults.clear();
			final Ray3 pickRay = new Ray3(p, normal);
			for (final Spatial spatial : collidables) {
				if (spatial != mesh) {
					PickingUtil.findPick(spatial, pickRay, pickResults, false);
					if (pickResults.getNumber() != 0) {
						break;
					}
				}
			}
			if (pickResults.getNumber() == 0) {
				reachable = true;
				break;
			}

		}

		userData.setReachable(reachable);

	}

}
