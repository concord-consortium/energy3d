package org.concord.energy3d.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.util.screen.ScreenExportable;

public class Printout implements ScreenExportable, Printable, Pageable {
//	protected boolean _useAlpha;
	private final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	private final PageFormat pageFormat;
	private final Dimension targetSize;
	private int currentX = 0;
	private int currentY = 0;

	public Printout(final PageFormat pageFormat, final Dimension targetSize) {
		this.pageFormat = pageFormat;
		this.targetSize = targetSize;
	}

//	@Override
//	public void export(final ByteBuffer data, final int width, final int height) {
//		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		images.add(img);
//
//		int index, r, g, b;
////		final int a;
//		int argb;
//		for (int x = 0; x < width; x++) {
//			for (int y = 0; y < height; y++) {
//				index = 3 * ((height - y - 1) * width + x);
//				r = data.get(index + 0);
//				g = data.get(index + 1);
//				b = data.get(index + 2);
//
//				argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
//
//				img.setRGB(x, y, argb);
//			}
//		}
//	}

	@Override
	public void export(final ByteBuffer data, final int width, final int height) {
		System.out.println("img width = " + width);
		final BufferedImage img;
		if (currentX == 0 && currentY == 0) {
			img = new BufferedImage(targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB);
			images.add(img);
		} else
			img = images.get(images.size() - 1);

		int index, r, g, b;
		int argb;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				index = 3 * ((height - y - 1) * width + x);
				r = data.get(index + 0);
				g = data.get(index + 1);
				b = data.get(index + 2);

				argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

				final int imgX = currentX + x;
				final int imgY = currentY + y;
				if (imgX < img.getWidth() && imgY < img.getHeight())
					img.setRGB(imgX, imgY, argb);
			}
		}
		currentX += width;
		if (currentX > targetSize.width) {
			currentX = 0;
			currentY += height;
		}
		if (currentY > targetSize.height)
			currentX = currentY = 0;

	}

//	public void export(final Image img) {
//		export(img.getData(0), img.getWidth(), img.getHeight());
//	}

//	public void export(final int[] data, final int width, final int height) {
//		final BufferedImage img = new BufferedImage(width, height, _useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
//		images.add(img);
//
//		int index, r, g, b, a;
//		int argb;
//		for (int x = 0; x < width; x++) {
//			for (int y = 0; y < height; y++) {
//				index = (_useAlpha ? 4 : 3) * ((height - y - 1) * width + x);
//				r = data[index + 0];
//				g = data[index + 1];
//				b = data[index + 2];
//
//				argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
//
//				if (_useAlpha) {
//					a = data[index + 3];
//					argb |= (a & 0xFF) << 24;
//				}
//				img.setRGB(x, y, argb);
//			}
//		}
//	}

	@Override
	public ImageDataFormat getFormat() {
//		if (_useAlpha) {
//			return ImageDataFormat.RGBA;
//		} else {
			return ImageDataFormat.RGB;
//		}
	}

//	public boolean isUseAlpha() {
//		return _useAlpha;
//	}
//
//	public void setUseAlpha(final boolean useAlpha) {
//		_useAlpha = useAlpha;
//	}

	@Override
	public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
		if (pageIndex >= images.size()) {
			return NO_SUCH_PAGE;
		}
		final BufferedImage img = images.get(pageIndex);
		graphics.drawImage(img, 0, 0, (int) pageFormat.getWidth(), (int) pageFormat.getHeight(), null);
//		graphics.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
		return PAGE_EXISTS;
	}

	@Override
	public Printable getPrintable(final int arg0) throws IndexOutOfBoundsException {
		return this;
	}

	@Override
	public PageFormat getPageFormat(final int arg0) throws IndexOutOfBoundsException {
		return pageFormat;
	}

	@Override
	public int getNumberOfPages() {
		return images.size();
	}

}
