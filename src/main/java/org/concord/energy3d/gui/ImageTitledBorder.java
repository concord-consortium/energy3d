package org.concord.energy3d.gui;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.border.TitledBorder;

import org.concord.energy3d.util.Config;

/**
 * @author Charles Xie
 * 
 */
public class ImageTitledBorder extends TitledBorder {

	private static final long serialVersionUID = 1L;
	private final Image image;

	public ImageTitledBorder(final String title, final Image image) {
		super(title);
		this.image = image;
	}

	@Override
	public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
		super.paintBorder(c, g, x, y, width, height);
		final FontMetrics fm = c.getFontMetrics(c.getFont());
		final int titleWidth = fm.stringWidth(getTitle());
		final int imageWidth = image.getWidth(c);
		g.drawImage(image, titleWidth - imageWidth - (Config.isMac() ? 6 : -2), Config.isMac() ? 2 : 0, imageWidth, image.getHeight(c), c);
	}

}