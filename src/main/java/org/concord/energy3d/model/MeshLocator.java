package org.concord.energy3d.model;

import java.io.Serializable;

import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;

/**
 * This allows a mesh to be found.
 * 
 * @author Charles Xie
 *
 */
public class MeshLocator implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Foundation foundation;
	private final int nodeIndex;
	private final int meshIndex;

	public MeshLocator(final Foundation foundation, final int nodeIndex, final int meshIndex) {
		this.foundation = foundation;
		this.nodeIndex = nodeIndex;
		this.meshIndex = meshIndex;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public int getMeshIndex() {
		return meshIndex;
	}

	public Mesh find() {
		if (foundation.getImportedNodes() == null) {
			return null;
		}
		final Node node = foundation.getImportedNodes().get(nodeIndex);
		if (node == null) {
			return null;
		}
		return (Mesh) node.getChild(meshIndex);
	}

}
