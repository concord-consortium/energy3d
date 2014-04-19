package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */
class ArrowIcon implements Icon {

	private int w, h;
	private Color color;

	ArrowIcon(int w, int h, Color color) {
		this.w = w;
		this.h = h;
		this.color = color;
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		g.setColor(color);
		final int x2 = w / 2;
		final int y2 = h / 2;
		final int[] vx = new int[] { 2, w - 2, x2 };
		final int[] vy = new int[] { y2 - 2, y2 - 2, y2 + 4 };
		g.fillPolygon(vx, vy, vx.length);
	}

	@Override
	public int getIconWidth() {
		return w;
	}

	@Override
	public int getIconHeight() {
		return h;
	}

}