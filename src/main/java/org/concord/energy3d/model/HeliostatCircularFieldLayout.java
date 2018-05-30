package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public class HeliostatCircularFieldLayout extends HeliostatFieldLayout {

	private double radialSpacing = 1;
	private double radialExpansionRatio = 0;
	private double azimuthalSpacing = 1;
	private int type = Foundation.EQUAL_AZIMUTHAL_SPACING;

	public void setType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setRadialSpacing(final double radialSpacing) {
		this.radialSpacing = radialSpacing;
	}

	public double getRadialSpacing() {
		return radialSpacing;
	}

	public void setRadialExpansionRatio(final double radialExpansionRatio) {
		this.radialExpansionRatio = radialExpansionRatio;
	}

	public double getRadialExpansionRatio() {
		return radialExpansionRatio;
	}

	public void setAzimuthalSpacing(final double azimuthalSpacing) {
		this.azimuthalSpacing = azimuthalSpacing;
	}

	public double getAzimuthalSpacing() {
		return azimuthalSpacing;
	}

}
