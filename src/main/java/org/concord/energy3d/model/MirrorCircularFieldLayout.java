package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public class MirrorCircularFieldLayout extends MirrorFieldLayout {

	private double radialSpacing = 1;
	private double radialSpacingIncrement = 0;
	private double azimuthalSpacing = 1;

	public void setRadialSpacing(final double radialSpacing) {
		this.radialSpacing = radialSpacing;
	}

	public double getRadialSpacing() {
		return radialSpacing;
	}

	public void setRadialSpacingIncrement(final double radialSpacingIncrement) {
		this.radialSpacingIncrement = radialSpacingIncrement;
	}

	public double getRadialSpacingIncrement() {
		return radialSpacingIncrement;
	}

	public void setAzimuthalSpacing(final double azimuthalSpacing) {
		this.azimuthalSpacing = azimuthalSpacing;
	}

	public double getAzimuthalSpacing() {
		return azimuthalSpacing;
	}

}
