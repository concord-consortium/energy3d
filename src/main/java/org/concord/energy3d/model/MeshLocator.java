package org.concord.energy3d.model;

import java.io.Serializable;

import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * This allows a mesh to be found.
 *
 * @author Charles Xie
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

    int getNodeIndex() {
        return nodeIndex;
    }

    public int getMeshIndex() {
        return meshIndex;
    }

    public Mesh find() {
        if (foundation.getImportedNodes() == null || foundation.getImportedNodes().isEmpty()) {
            return null;
        }
        Node node = null;
        for (final Node n : foundation.getImportedNodes()) {
            if (n.getNumberOfChildren() > 0) {
                final Spatial s = n.getChild(0);
                final Mesh m = (Mesh) s;
                final UserData u = (UserData) m.getUserData();
                if (u.getNodeIndex() == nodeIndex) {
                    node = n;
                    break;
                }
            }
        }
        if (node != null) {
            for (final Spatial s : node.getChildren()) {
                final Mesh m = (Mesh) s;
                final UserData u = (UserData) m.getUserData();
                if (u.getMeshIndex() == meshIndex) {
                    return m;
                }
            }
        }
        return null;
    }

}