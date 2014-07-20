package org.concord.energy3d.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

/**
 * @author Charles Xie
 * 
 */

public abstract class Symbol implements Icon {

	protected int x = 0, y = 0, w = 8, h = 8;
	protected Color color = Color.white;
	protected Stroke stroke = new BasicStroke(1);
	protected boolean paintBorder;
	protected boolean pressed;
	protected boolean disabled;

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setBorderPainted(boolean paintBorder) {
		this.paintBorder = paintBorder;
	}

	public boolean isBorderPainted() {
		return paintBorder;
	}

	public void setIconWidth(int width) {
		w = width;
	}

	public int getIconWidth() {
		return w;
	}

	public void setIconHeight(int height) {
		h = height;
	}

	public int getIconHeight() {
		return h;
	}

	public boolean contains(int rx, int ry) {
		return rx > x && rx < x + w && ry > y && ry < y + h;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		this.x = x;
		this.y = y;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		if (paintBorder) {
			g.drawRoundRect(x, y, w, h, 10, 10);
		}
		g2.setStroke(stroke);
	}

	public Symbol getScaledInstance(float scale) {
		try {
			Symbol icon = getClass().newInstance();
			icon.setIconWidth((int) (scale * icon.getIconWidth()));
			icon.setIconHeight((int) (scale * icon.getIconHeight()));
			return icon;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Image createImage(Component c) {
		BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		try {
			paintIcon(c, g, 0, 0);
			return image;
		} finally {
			g.dispose();
		}
	}

	static class Arrow extends Symbol {

		public Arrow(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
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

	}

	static class Moon extends Symbol {

		public Moon(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Area a = new Area(new Ellipse2D.Float(x, y, w, h));
			a.subtract(new Area(new Ellipse2D.Float(x + w * 0.25f, y, w, h)));
			g2.fill(a);
		}

	}

	static class Sun extends Symbol {

		public Sun(Color color, int w, int h) {
			setColor(color);
			setIconWidth(w);
			setIconHeight(h);
			setStroke(new BasicStroke(2));
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			Ellipse2D.Float s = new Ellipse2D.Float(x, y, w * 0.75f, h * 0.75f);
			g2.fill(s);
			int x1, y1;
			double angle = 0;
			int n = 8;
			for (int i = 0; i < n; i++) {
				angle = i * Math.PI * 2 / (double) n;
				x1 = (int) Math.round(s.getCenterX() + w / 2 * Math.cos(angle));
				y1 = (int) Math.round(s.getCenterY() + h / 2 * Math.sin(angle));
				g2.drawLine(x1, y1, (int) Math.round(s.getCenterX()), (int) Math.round(s.getCenterY()));
			}
		}

	}

	static class Thermometer extends Symbol {

		// since there can be many thermometers, we want to make a singleton.
		private final static Thermometer instance = new Thermometer();

		public static Thermometer sharedInstance() {
			return instance;
		}

		private int value;
		private int ballDiameter;

		public Thermometer() {
		}

		public void setValue(int value) {
			this.value = value;
		}

		public int getBarHeight() {
			return h - Math.round(w * 1.5f);
		}

		public int getBallDiameterOffset() {
			return ballDiameter - 3;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			super.paintIcon(c, g, x, y);
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.white);
			g2.fillRect(x, y, w, h);
			ballDiameter = Math.round(w * 1.45f);
			int x2 = x + w / 2;
			int y2 = y + h - ballDiameter + 2;
			if (value != 0) {
				g2.setColor(Color.red);
				BasicStroke bs = new BasicStroke(getIconWidth() * 0.3f);
				g2.setStroke(bs);
				g2.drawLine(x2, y2 - value, x2, y2);
			}
			g2.setColor(Color.black);
			g2.setStroke(stroke);
			g2.drawRect(x, y, w, h);
			int n = h / 2;
			for (int i = 1; i < n; i++) {
				g2.drawLine(x, y + i * 2, Math.round(x + 0.2f * w), y + i * 2);
				g2.drawLine(x + w, y + i * 2, Math.round(x + w - 0.2f * w), y + i * 2);
			}
			x2 = Math.round(x - w * 0.25f);
			g2.setColor(Color.lightGray);
			g2.fillOval(x2, y2, ballDiameter, ballDiameter);
			g2.setColor(Color.black);
			g2.drawOval(x2, y2, ballDiameter, ballDiameter);
		}

	}

}
