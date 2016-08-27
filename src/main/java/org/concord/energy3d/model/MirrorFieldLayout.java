package org.concord.energy3d.model;

/**
 * @author Charles Xie
 *
 */
public abstract class MirrorFieldLayout {

	double mirrorWidth = 2;
	double mirrorHeight = 3;
	double startAngle = 0;
	double endAngle = 360;
	double axisRoadWidth = 0;

	public void setMirrorWidth(final double mirrorWidth) {
		this.mirrorWidth = mirrorWidth;
	}

	public double getMirrorWidth() {
		return mirrorWidth;
	}

	public void setMirrorHeight(final double mirrorHeight) {
		this.mirrorHeight = mirrorHeight;
	}

	public double getMirrorHeight() {
		return mirrorHeight;
	}

	public void setStartAngle(final double startAngle) {
		this.startAngle = startAngle;
	}

	public double getStartAngle() {
		return startAngle;
	}

	public void setEndAngle(final double endAngle) {
		this.endAngle = endAngle;
	}

	public double getEndAngle() {
		return endAngle;
	}

	public void setAxisRoadWidth(final double axisRoadWidth) {
		this.axisRoadWidth = axisRoadWidth;
	}

	public double getAxisRoadWidth() {
		return axisRoadWidth;
	}

}
