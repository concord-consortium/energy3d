package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
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
		setBackground(Color.BLACK);
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
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		if (image != null) {
			g2.drawImage(image, 0, 0, this);
		}
		if (text != null) {
			g2.setFont(new Font(null, Font.BOLD, 14));
			final FontMetrics fm = g2.getFontMetrics();
			final int textWidth = fm.stringWidth(text);
			final int x = (getWidth() - textWidth) / 2;
			final int y = getHeight() / 2;
			g2.setColor(Color.BLACK);
			g2.fillRect(x - 8, y - 14, textWidth + 16, 20);
			g2.setColor(textColor);
			g2.drawString(text, x, y);
		}
	}

	final static String getGoogleMapUrl(final String mapType, final double latitude, final double longitude, final int zoom, final int width, final int height) {
		return "https://maps.googleapis.com/maps/api/staticmap?maptype=" + mapType + "&center=" + latitude + "," + longitude + "&zoom=" + zoom + "&size=" + width + "x" + height + "&scale=1&key=AIzaSyBEGiCg33CccHloDdPENWk1JDhwTEQaZQ0";
	}

}
