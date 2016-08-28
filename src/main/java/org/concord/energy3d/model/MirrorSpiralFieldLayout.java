package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public class MirrorSpiralFieldLayout extends MirrorFieldLayout {

	private int type = Foundation.FERMAT_SPIRAL;
	private int startTurn = 10;
	private double scalingFactor = 0.8;

	public void setType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
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
