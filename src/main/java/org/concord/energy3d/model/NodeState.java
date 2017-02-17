package org.concord.energy3d.model;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * This class holds the state of an imported node for persistence.
 * 
 * @author Charles Xie
 *
 */
public class NodeState implements Serializable {

	private static final long serialVersionUID = 1L;

	private Vector3 relativePosition; // relative to the center of the foundation
	private Vector3 absolutePosition; // save the absolute position as we need it to nail down the position of this node when the foundation is resized
	private ReadOnlyColorRGBA defaultColor = ColorRGBA.WHITE;
	private URL sourceURL;
	private String name;
	private ArrayList<Integer> reversedFaceMeshes;
	private ArrayList<Integer> deletedMeshes;
	private double meshThickness = 0.05; // give the twin meshes imported from SketchUp and other CAD software some thickness

	public NodeState() {
	}

	public NodeState clone() {
		final NodeState copy = new NodeState();
		copy.relativePosition = relativePosition != null ? relativePosition.clone() : null;
		copy.defaultColor = defaultColor.clone();
		copy.sourceURL = sourceURL;
		copy.name = name;
		copy.meshThickness = meshThickness;
		if (reversedFaceMeshes != null) {
			copy.reversedFaceMeshes = new ArrayList<Integer>();
			copy.reversedFaceMeshes.addAll(reversedFaceMeshes);
		}
		if (deletedMeshes != null) {
			copy.deletedMeshes = new ArrayList<Integer>();
			copy.deletedMeshes.addAll(deletedMeshes);
		}
		return copy;
	}

	public void reverseNormalOfMesh(final int index) {
		if (reversedFaceMeshes == null) {
			reversedFaceMeshes = new ArrayList<Integer>();
		}
		if (reversedFaceMeshes.contains(index)) {
			reversedFaceMeshes.remove(Integer.valueOf(index));
		} else {
			reversedFaceMeshes.add(index);
		}
	}

	public ArrayList<Integer> getMeshesWithReversedNormal() {
		return reversedFaceMeshes;
	}

	public void deleteMesh(final int index) {
		if (deletedMeshes == null) {
			deletedMeshes = new ArrayList<Integer>();
		}
		if (!deletedMeshes.contains(index)) {
			deletedMeshes.add(index);
		}
	}

	public ArrayList<Integer> getDeletedMeshes() {
		return deletedMeshes;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/** relative to the center of the foundation */
	public void setRelativePosition(final Vector3 relativePosition) {
		this.relativePosition = relativePosition;
	}

	/** relative to the center of the foundation */
	public Vector3 getRelativePosition() {
		return relativePosition;
	}

	public void setAbsolutePosition(final Vector3 absolutePosition) {
		this.absolutePosition = absolutePosition;
	}

	public Vector3 getAbsolutePosition() {
		return absolutePosition;
	}

	public void setDefaultColor(final ReadOnlyColorRGBA defaultColor) {
		this.defaultColor = defaultColor;
	}

	public ReadOnlyColorRGBA getDefaultColor() {
		return defaultColor;
	}

	public void setSourceURL(final URL sourceURL) {
		this.sourceURL = sourceURL;
	}

	public URL getSourceURL() {
		return sourceURL;
	}

	public void setMeshThickness(final double thickness) {
		meshThickness = thickness;
	}

	public double getMeshThickness() {
		return meshThickness;
	}

}
