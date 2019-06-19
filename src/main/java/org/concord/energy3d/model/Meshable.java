package org.concord.energy3d.model;

/**
 * This interface defines a type of objects that recognizes its underlying mesh from an imported structure.
 *
 * @author Charles Xie
 */
public interface Meshable {

    MeshLocator getMeshLocator();

    void setMeshLocator(MeshLocator meshLocator);

}