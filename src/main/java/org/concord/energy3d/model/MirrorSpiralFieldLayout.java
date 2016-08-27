package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public class MirrorSpiralFieldLayout extends MirrorFieldLayout {

	private int spiralType = Foundation.FERMAT_SPIRAL;
	private int startTurn = 10;
	private double scalingFactor = 1;

	public void setSpiralType(final int spiralType) {
		this.spiralType = spiralType;
	}

	public int getSpiralType() {
		return spiralType;
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

}
