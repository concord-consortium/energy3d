package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JSlider;

import org.concord.energy3d.simulation.SolarRadiation;

import com.ardor3d.math.ColorRGBA;

/**
 * @author Charles Xie
 * 
 */
class MySlider extends JSlider {

	private static final long serialVersionUID = 1L;

	MySlider() {
		setOpaque(false);
		setPaintTicks(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		final int STEP = 5;
		final Dimension size = getSize();
		final int y1 = 2 * size.height / 3;
		for (int x = 0; x < size.width - STEP; x += STEP) {
			final ColorRGBA color = SolarRadiation.computeColor(x, size.width);
			g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
			g.fillRect(x, 0, x + STEP, y1);
		}
		g.setColor(Color.WHITE);
		g.fillRect(0, y1, size.width - 1, size.height - y1 - 1);
		g.setColor(Color.GRAY);
		g.drawRect(0, y1, size.width - 1, size.height - y1 - 1);
		super.paintComponent(g);
	}
}