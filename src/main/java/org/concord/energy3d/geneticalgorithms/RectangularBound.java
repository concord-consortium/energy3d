package org.concord.energy3d.geneticalgorithms;

import java.awt.geom.Rectangle2D;

/**
 * @author Charles Xie
 */
public class RectangularBound implements Constraint {

    private final Rectangle2D.Double rectangle;

    public RectangularBound(final double centerX, final double centerY, final double width, final double height) {
        rectangle = new Rectangle2D.Double(centerX - width * 0.5, centerY - height * 0.5, width, height);
    }

    public boolean contains(final double x, final double y) {
        return rectangle.contains(x, y);
    }

}