package org.concord.energy3d.model;

import java.awt.EventQueue;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
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
public class NodeWorker {

	private final Node node;
	private final List<Spatial> collidables;
	private final Map<Mesh, OrientedBoundingBox> meshToBox;

	public NodeWorker(final Node node) {
		this.node = node;
		collidables = new ArrayList<Spatial>();
		collidables.addAll(node.getChildren());
		collidables.add(SceneManager.getInstance().getLand());
		meshToBox = new HashMap<Mesh, OrientedBoundingBox>();
	}

	void work(final boolean remove) {
		// offsetTwinMeshes();
		int count = 0;
		for (final Spatial s : node.getChildren()) {
			reach((Mesh) s);
			if (count % 20 == 0) {
				EnergyPanel.getInstance().progress((int) Math.round(100.0 * count / node.getNumberOfChildren()));
			}
			count++;
		}
		indexify();
		if (remove) {
			removeInteriorMeshes();
		}
		EnergyPanel.getInstance().progress(0);
	}

	void findTwinMeshes() {
		final double offset = Scene.getInstance().getMeshThickness();
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
				if (Util.isEqual(b1, b2, 0.001)) { // TODO: Use bounding box equality to detect mesh vertex equality is questionable
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

	public void setThickness(final double thickness) {
		if (Util.isEqual(thickness, Scene.getInstance().getMeshThickness())) {
			return;
		}
		for (final Spatial s : node.getChildren()) {
			final Mesh m = (Mesh) s;
			final UserData u = (UserData) m.getUserData();
			m.addTranslation((u.getRotatedNormal() == null ? u.getNormal() : u.getRotatedNormal()).multiply(thickness - Scene.getInstance().getMeshThickness(), null));
		}
		Scene.getInstance().setMeshThickness(thickness);
	}

	// If a ray in the direction of the normal of this mesh doesn't hit anything, it is considered as an exterior face of a twin mesh. Otherwise, it is considered as the interior face.
	private void reach(final Mesh mesh) {
		final UserData userData = (UserData) mesh.getUserData();
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
		p.multiplyLocal(1.0 / vertices.size()).addLocal(normal.multiply(Scene.getInstance().getMeshThickness(), null));

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
	}

	private void resetSideIndex() {
		for (final Spatial s : node.getChildren()) {
			final Mesh m = (Mesh) s;
			final UserData u = (UserData) m.getUserData();
			u.setFaceeIndex(0);
		}
	}

	private void indexify() {
		resetSideIndex();
		for (final Spatial s : node.getChildren()) {
			final Mesh m = (Mesh) s;
			final UserData u = (UserData) m.getUserData();
			if (u.getFaceIndex() == 0) { // side index = 0 means it hasn't been set or should not be set
				if (u.isReachable()) { // the ray can reach the center of this mesh
					final Mesh mTwin = u.getTwin();
					if (mTwin != null) {
						final UserData uTwin = (UserData) mTwin.getUserData();
						if (!uTwin.isReachable()) {
							uTwin.setFaceeIndex(-1);
							u.setFaceeIndex(1);
						}
					} else {
						u.setFaceeIndex(1);
					}
				} else {
					// we are not sure about the rest of the cases
				}
			}
		}
	}

	private void removeInteriorMeshes() {
		final List<Mesh> toRemove = new ArrayList<Mesh>();
		for (final Spatial s : node.getChildren()) {
			final Mesh m = (Mesh) s;
			final UserData u = (UserData) m.getUserData();
			if (u.getFaceIndex() == -1) {
				toRemove.add(m);
			}
		}
		for (final Mesh m : toRemove) {
			node.detachChild(m);
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), toRemove.size() + " interior meshes were removed", "Node Worker", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	public static void reverseFace(final Mesh m) {
		if (m == null) {
			return;
		}
		final UserData u = (UserData) m.getUserData();
		u.setNormal(u.getNormal().negate(null));
		if (u.getRotatedNormal() != null) {
			u.setRotatedNormal(u.getRotatedNormal().negate(null));
		}
	}

}
