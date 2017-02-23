package org.concord.energy3d.model;

/**
 * 
 * This interface defines a type of objects that recognizes its underlying mesh from an imported structure.
 * 
 * @author Charles Xie
 *
 */
public interface Meshable {

	public MeshLocator getMeshLocator();

	public void setMeshLocator(MeshLocator meshLocator);

}
