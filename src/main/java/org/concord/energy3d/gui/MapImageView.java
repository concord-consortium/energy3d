package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * TODO: This view should simulate SketchUp's behavior. It will be enhanced in the future.
 * 
 * @author Charles Xie
 *
 */
class MapImageView extends JComponent {

	private static final long serialVersionUID = 1L;
	private Image image;
	private String text;
	private Color textColor = Color.WHITE;
	private final Point point;

	MapImageView() {
		super();
		setFocusable(true);
		setRequestFocusEnabled(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		point = new Point();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				requestFocusInWindow();
				point.setLocation(e.getPoint());
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				point.setLocation(0, 0);
			}
		});
	}

	Point getPoint() {
		return point;
	}

	void setText(final String text) {
		this.text = text;
	}

	void setTextColor(final Color textColor) {
		this.textColor = textColor;
	}

	void setImage(final Image image) {
		this.image = image;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		if (image != null) {
			g2.drawImage(image, 0, 0, this);
		}
		if (text != null) {
			final FontMetrics fm = g2.getFontMetrics();
			final int textWidth = fm.stringWidth(text);
			g2.setFont(new Font(null, Font.BOLD, 14));
			g2.setColor(textColor);
			g2.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
		}
	}

	final static String getGoogleMapUrl(final String mapType, final boolean highResolution, final double latitude, final double longitude, final int zoom) {
		final int scale = highResolution & zoom <= 20 ? 2 : 1;
		return "https://maps.googleapis.com/maps/api/staticmap?maptype=" + mapType + "&center=" + latitude + "," + longitude + "&zoom=" + zoom + "&size=640x640&scale=" + scale + "&key=AIzaSyBEGiCg33CccHloDdPENWk1JDhwTEQaZQ0";
	}

}
