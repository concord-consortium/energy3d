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
 */
class MapImageViewWithLocations extends MapImageView {

    private static final long serialVersionUID = 1L;

    MapImageViewWithLocations() {
        super();
        final Dimension d = new Dimension(512, 512);
        setPreferredSize(d);
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final int width = getWidth();
        final int height = getHeight();
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
            ellipse.width = 1;
            ellipse.height = 1;
            ellipse.x = x - ellipse.width * 0.5;
            ellipse.y = y - ellipse.height * 0.5;
            g2.setColor(Color.YELLOW);
            g2.fill(ellipse);
            g2.setColor(Color.DARK_GRAY);
            ellipse.width += 1;
            ellipse.height += 1;
            ellipse.x = x - ellipse.width * 0.5;
            ellipse.y = (s.equals("Amundsen-Scott Station") ? height - ellipse.height : y) - ellipse.height * 0.5; // special case, lift south pole up a bit
            g2.draw(ellipse);
        }
        final String current = (String) EnergyPanel.getInstance().getRegionComboBox().getSelectedItem();
        if (!current.equals("")) {
            latitude = LocationData.getInstance().getLatitudes().get(current);
            longitude = LocationData.getInstance().getLongitudes().get(current);
            x = (longitude + 180.0) * (width / 360.0);
            y = height * 0.5 - width * Math.log(Math.tan(0.25 * Math.PI + 0.5 * latitude * Math.PI / 180.0)) / (2 * Math.PI);
            ellipse.width = 5;
            ellipse.height = 5;
            ellipse.x = x - ellipse.width * 0.5;
            ellipse.y = (current.equals("Amundsen-Scott Station") ? height - ellipse.height : y) - ellipse.height * 0.5; // special case, lift south pole up a bit
            g2.setColor(Color.RED);
            g2.fill(ellipse);
            g2.setColor(Color.BLACK);
            ellipse.width += 1;
            ellipse.height += 1;
            ellipse.x = x - ellipse.width * 0.5;
            ellipse.y = (current.equals("Amundsen-Scott Station") ? height - ellipse.height : y) - ellipse.height * 0.5; // special case, lift south pole up a bit
            g2.draw(ellipse);
        }
    }

}