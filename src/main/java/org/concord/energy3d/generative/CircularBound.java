package org.concord.energy3d.generative;

import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 *
 */
public class CircularBound implements Constraint {

	private final Ellipse2D.Double circle;
	private final boolean included;

	public CircularBound(final double centerX, final double centerY, final double radius, final boolean included) {
		circle = new Ellipse2D.Double(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
		this.included = included;
	}

	public boolean meet(final double x, final double y) {
		return included ? circle.contains(x, y) : !circle.contains(x, y);
	}

}
