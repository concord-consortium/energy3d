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

	private Vector3 position; // relative to the center of the foundation
	private ReadOnlyColorRGBA defaultColor = ColorRGBA.WHITE;
	private URL sourceURL;
	private String name;
	private ArrayList<Integer> reversedFaceMeshes;

	public NodeState() {
	}

	public NodeState makeCopy() {
		final NodeState copy = new NodeState();
		copy.position = position != null ? position.clone() : null;
		copy.defaultColor = defaultColor.clone();
		copy.sourceURL = sourceURL;
		copy.name = name;
		if (reversedFaceMeshes != null) {
			copy.reversedFaceMeshes = new ArrayList<Integer>();
			copy.reversedFaceMeshes.addAll(reversedFaceMeshes);
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

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/** relative to the center of the foundation */
	public void setPosition(final Vector3 position) {
		this.position = position;
	}

	/** relative to the center of the foundation */
	public Vector3 getPosition() {
		return position;
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

}
