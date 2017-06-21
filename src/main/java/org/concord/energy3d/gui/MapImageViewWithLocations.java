package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Set;

import org.concord.energy3d.simulation.LocationData;

/**
 * This map view will show supported locations.
 * 
 * @author Charles Xie
 *
 */
class MapImageViewWithLocations extends MapImageView {

	private static final long serialVersionUID = 1L;
	private final int offsetX;
	private final int offsetY;

	MapImageViewWithLocations() {
		super();
		final Dimension d = new Dimension(600, 600);
		setPreferredSize(d);
		offsetX = d.width / 10;
		offsetY = d.height / 10;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final int width = getWidth() - offsetX * 2;
		final int height = getHeight() - offsetY * 2;
		final Set<String> locationNames = LocationData.getInstance().getLatitudes().keySet();
		float latitude, longitude;
		double x;
		double y;
		final Graphics2D g2 = (Graphics2D) g;
		final Ellipse2D.Double ellipse = new Ellipse2D.Double();
		for (final String s : locationNames) { // apply the Mercator projection
			latitude = LocationData.getInstance().getLatitudes().get(s);
			longitude = LocationData.getInstance().getLongitudes().get(s);
			x = (longitude + 180.0) * (width / 360.0);
			y = height * 0.5 - width * Math.log(Math.tan(0.25 * Math.PI + 0.5 * latitude * Math.PI / 180.0)) / (2 * Math.PI);
			ellipse.x = x + offsetX;
			ellipse.y = y + offsetY;
			ellipse.width = 2;
			ellipse.height = 2;
			g2.setColor(Color.YELLOW);
			g2.fill(ellipse);
			g2.setColor(Color.BLACK);
			ellipse.width = 3;
			ellipse.height = 3;
			g2.draw(ellipse);
		}
	}

}
