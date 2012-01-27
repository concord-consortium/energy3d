package org.concord.energy3d.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.util.screen.ScreenExportable;

public class Printout implements ScreenExportable, Printable, Pageable {
	protected boolean _useAlpha;
	private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	
	final PageFormat pageFormat; // = new PageFormat();
//	final Paper paper; // = new Paper();

	public Printout(final PageFormat pageFormat) {
//		this(false);
//		this.pa
//		paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
//		pageFormat.setPaper(paper);
		this.pageFormat = pageFormat;
	}

//	public Printout(boolean useAlpha) {
//		_useAlpha = useAlpha;
//	}

	public void export(final ByteBuffer data, final int width, final int height) {
		final BufferedImage img = new BufferedImage(width, height, _useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		images.add(img);

		int index, r, g, b, a;
		int argb;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				index = (_useAlpha ? 4 : 3) * ((height - y - 1) * width + x);
				r = data.get(index + 0);
				g = data.get(index + 1);
				b = data.get(index + 2);

				argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

				if (_useAlpha) {
					a = ((data.get(index + 3)));
					argb |= (a & 0xFF) << 24;
				}
				img.setRGB(x, y, argb);
			}
		}
	}
	
	public void export(final int[] data, final int width, final int height) {
		final BufferedImage img = new BufferedImage(width, height, _useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		images.add(img);

		int index, r, g, b, a;
		int argb;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				index = (_useAlpha ? 4 : 3) * ((height - y - 1) * width + x);
				r = data[index + 0];
				g = data[index + 1];
				b = data[index + 2];

				argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

				if (_useAlpha) {
					a = data[index + 3];
					argb |= (a & 0xFF) << 24;
				}
				img.setRGB(x, y, argb);
			}
		}
	}	

	public ImageDataFormat getFormat() {
		if (_useAlpha) {
			return ImageDataFormat.RGBA;
		} else {
			return ImageDataFormat.RGB;
		}
	}

	public boolean isUseAlpha() {
		return _useAlpha;
	}

	public void setUseAlpha(final boolean useAlpha) {
		_useAlpha = useAlpha;
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex >= images.size()) {
			return NO_SUCH_PAGE;
		}
		final BufferedImage img = images.get(pageIndex);
		
//		final int w = img.getWidth();
//		final int h = img.getHeight();
//		final double pageRatio = pageFormat.getImageableWidth() / pageFormat.getImageableHeight();
//		final BufferedImage croppedImg;
//		if (w / h > pageRatio)
//			croppedImg = img.getSubimage((int)(w-h * pageRatio)/2, 0, (int)(h * pageRatio), h);
//		else
//			croppedImg = img.getSubimage(0, (int)(h-w / pageRatio)/2, w, (int)(w / pageRatio));
		
//		graphics.drawImage(croppedImg, 0, 0, (int)pageFormat.getWidth(), (int)pageFormat.getHeight() , null);
//		graphics.drawImage(img, (int) pageFormat.getImageableX(), (int) pageFormat.getImageableY(), (int) pageFormat.getWidth(), (int) pageFormat.getHeight() , null);
		graphics.drawImage(img, 0, 0, (int)pageFormat.getWidth(), (int)pageFormat.getHeight() , null);
		return PAGE_EXISTS;
	}
	

		@Override
		public Printable getPrintable(int arg0) throws IndexOutOfBoundsException {
			return this;
		}

		@Override
		public PageFormat getPageFormat(int arg0) throws IndexOutOfBoundsException {
			return pageFormat;
		}

		@Override
		public int getNumberOfPages() {
			return images.size();
		}
	
}
