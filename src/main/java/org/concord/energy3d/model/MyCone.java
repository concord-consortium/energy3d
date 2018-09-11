package org.concord.energy3d.model;

public class MyCone extends MyCylinder {

	public MyCone() {
	}

	public MyCone(final String name, final int axisSamples, final int radialSamples, final float radius, final float height) {
		this(name, axisSamples, radialSamples, radius, height, true);
	}

	public MyCone(final String name, final int axisSamples, final int radialSamples, final float radius, final float height, final boolean closed) {
		super(name, axisSamples, radialSamples, radius, height, closed);
		setRadius2(0);
	}

	public void setHalfAngle(final float radians) {
		setRadius1(Math.tan(radians));
	}
}
