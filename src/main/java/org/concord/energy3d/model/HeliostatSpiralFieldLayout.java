package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public class HeliostatSpiralFieldLayout extends HeliostatFieldLayout {

	public final static double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5)); // in degrees
	public final static int MAXIMUM_NUMBER_OF_HELIOSTATS = 10000;

	private int type = Foundation.FERMAT_SPIRAL;
	private double divergence = GOLDEN_ANGLE;
	private int startTurn = 3;
	private double scalingFactor = 0.5;
	private double radialExpansionRatio;

	public void setType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setDivergence(final double divergence) {
		this.divergence = divergence;
	}

	public double getDivergence() {
		return divergence;
	}

	public void setStartTurn(final int startTurn) {
		this.startTurn = startTurn;
	}

	public int getStartTurn() {
		return startTurn;
	}

	public void setScalingFactor(final double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	public double getScalingFactor() {
		return scalingFactor;
	}

	public void setRadialExpansionRatio(final double radialExpansionRatio) {
		this.radialExpansionRatio = radialExpansionRatio;
	}

	public double getRadialExpansionRatio() {
		return radialExpansionRatio;
	}

}
