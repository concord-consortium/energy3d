package org.concord.energy3d.gui;

import java.awt.Cursor;
import java.awt.Graphics;
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

	void setImage(final Image image) {
		this.image = image;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}

}
