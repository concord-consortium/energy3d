package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * This class processes the meshes imported from other CAD software.
 * 
 * @author Charles Xie
 *
 */
class NodeWorker {

	private final Node node;
	private final List<Spatial> collidables;
	private final Map<Mesh, OrientedBoundingBox> meshToBox;
	private final double offset = 0.05;

	NodeWorker(final Node node) {
		this.node = node;
		collidables = new ArrayList<Spatial>();
		collidables.addAll(node.getChildren());
		collidables.add(SceneManager.getInstance().getLand());
		meshToBox = new HashMap<Mesh, OrientedBoundingBox>();
	}

	void work() {
		// offsetTwinMeshes();
		int count = 0;
		for (final Spatial s : node.getChildren()) {
			processMesh((Mesh) s);
			if (count % 20 == 0) {
				EnergyPanel.getInstance().progress((int) Math.round(100.0 * count / node.getNumberOfChildren()));
			}
			count++;
		}
		removeInteriorMeshes();
		EnergyPanel.getInstance().progress(0);
	}

	void offsetTwinMeshes() {
		final int n = node.getNumberOfChildren();
		for (int i1 = 0; i1 < n; i1++) {
			final Mesh m1 = (Mesh) node.getChild(i1);
			final UserData u1 = (UserData) m1.getUserData();
			final OrientedBoundingBox b1 = Util.getOrientedBoundingBox(m1);
			meshToBox.put(m1, b1);
			for (int i2 = i1 + 1; i2 < n; i2++) {
				final Mesh m2 = (Mesh) node.getChild(i2);
				final OrientedBoundingBox b2;
				if (meshToBox.get(m2) == null) {
					b2 = Util.getOrientedBoundingBox(m2);
					meshToBox.put(m2, b2);
				} else {
					b2 = meshToBox.get(m2);
				}
				if (Util.isEqual(b1, b2, 0.001)) {
					final UserData u2 = (UserData) m2.getUserData();
					m1.addTranslation((u1.getRotatedNormal() == null ? u1.getNormal() : u1.getRotatedNormal()).multiply(offset, null));
					m2.addTranslation((u2.getRotatedNormal() == null ? u2.getNormal() : u2.getRotatedNormal()).multiply(offset, null));
					u1.setTwin(m2);
					u2.setTwin(m1);
					break;
				}
			}
			if (i1 % 20 == 0) {
				EnergyPanel.getInstance().progress((int) Math.round(100.0 * i1 / n));
			}
		}
		EnergyPanel.getInstance().progress(0);
	}

	private void processMesh(final Mesh mesh) {

		final UserData userData = (UserData) mesh.getUserData();
		if (userData.getSideIndex() != 0) { // this mesh has already been processed
			return;
		}
		final ReadOnlyVector3 normal = userData.getRotatedNormal() == null ? userData.getNormal() : userData.getRotatedNormal();

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		final List<ReadOnlyVector3> vertices = new ArrayList<ReadOnlyVector3>(vertexBuffer.limit() / 3);
		while (vertexBuffer.hasRemaining()) {
			vertices.add(mesh.localToWorld(new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()), null));
		}
		final Vector3 p = new Vector3();
		for (final ReadOnlyVector3 v : vertices) {
			p.addLocal(v);
		}
		// we must apply the offset transfer as these points come from the vertex buffer that is not affected by the translation definition of the mesh
		p.multiplyLocal(1.0 / vertices.size()).addLocal((userData.getRotatedNormal() == null ? userData.getNormal() : userData.getRotatedNormal()).multiply(offset, null));

		final Ray3 pickRay = new Ray3(p, normal);
		final PickResults pickResults = new PrimitivePickResults();
		for (final Spatial spatial : collidables) {
			if (spatial != mesh) {
				PickingUtil.findPick(spatial, pickRay, pickResults, false);
				if (pickResults.getNumber() != 0) {
					break;
				}
			}
		}
		userData.setReachable(pickResults.getNumber() == 0);
		if (userData.isReachable()) { // the ray can reach the center of this mesh, mark it as an exterior face
			if (userData.getSideIndex() == 0) { // side index = 0 means it hasn't been set, set it to 1 to indicate it is an exterior face
				userData.setSideIndex(1);
				final Mesh twin = userData.getTwin(); // meanwhile, mark its twin (if any) as an interior face
				if (twin != null) {
					final UserData uTwin = (UserData) twin.getUserData();
					uTwin.setSideIndex(-1);
				}
			}
		}
	}

	private void removeInteriorMeshes() {
		final List<Mesh> toRemove = new ArrayList<Mesh>();
		for (final Spatial s : node.getChildren()) {
			final Mesh m = (Mesh) s;
			final UserData u = (UserData) m.getUserData();
			if (u.getSideIndex() == -1) {
				toRemove.add(m);
			}
		}
		for (final Mesh m : toRemove) {
			node.detachChild(m);
		}
		System.out.println("NodeWorker: " + toRemove.size() + " interior meshes removed");
	}

}
