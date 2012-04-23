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

import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.ImageDataType;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.util.geom.BufferUtils;

public class Printout implements Printable, Pageable {
	// protected boolean _useAlpha;
//	private final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	private final PageFormat pageFormat;
	private final Dimension targetSize;
//	private int currentX = 0;
//	private int currentY = 0;
	private ByteBuffer _scratch; // = BufferUtils.createByteBuffer(1);
	final private double visibleSceneWidth;
	final private double visibleSceneHeight;
	private final BufferedImage image; // = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);;
	private final ArrayList<ReadOnlyVector3> printCorners;
	private int lastImagePage = -1;

	public Printout(final PageFormat pageFormat, final Dimension targetSize, final double visibleSceneWidth, final double visibleSceneHeight, final ArrayList<ReadOnlyVector3> printCorners) {
		this.pageFormat = pageFormat;
		this.targetSize = targetSize;
		this.visibleSceneWidth = visibleSceneWidth;
		this.visibleSceneHeight = visibleSceneHeight;
		this.printCorners = printCorners;
		final Camera camera = Camera.getCurrentCamera();
		_scratch = BufferUtils.createByteBuffer(camera.getWidth() * camera.getHeight() * ImageUtils.getPixelByteSize(getFormat(), ImageDataType.UnsignedByte));
		image = new BufferedImage(targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB);

//		final Vector3 cameraLocation = new Vector3(printCorners.get(0)).addLocal(visibleSceneWidth / 2.0, -10.0, -visibleSceneHeight / 2.0);
//
//			camera.setLocation(cameraLocation);
//			camera.lookAt(cameraLocation.add(0, 1, 0, null), Vector3.UNIT_Z);
//			SceneManager.getInstance().getCameraNode().updateFromCamera();
//
//		SceneManager.getInstance().frameHandler.updateFrame();
	}

	// @Override
	// public void export(final ByteBuffer data, final int width, final int height) {
	// final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	// images.add(img);
	//
	// int index, r, g, b;
	// // final int a;
	// int argb;
	// for (int x = 0; x < width; x++) {
	// for (int y = 0; y < height; y++) {
	// index = 3 * ((height - y - 1) * width + x);
	// r = data.get(index + 0);
	// g = data.get(index + 1);
	// b = data.get(index + 2);
	//
	// argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
	//
	// img.setRGB(x, y, argb);
	// }
	// }
	// }

//	@Override
//	public void export(final ByteBuffer data, final int width, final int height) {
//		System.out.println("img width = " + width);
//		final BufferedImage img;
//		if (currentX == 0 && currentY == 0) {
//			img = new BufferedImage(targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB);
//			images.add(img);
//		} else
//			img = images.get(images.size() - 1);
//
//		int index, r, g, b;
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
//				final int imgX = currentX + x;
//				final int imgY = currentY + y;
//				if (imgX < img.getWidth() && imgY < img.getHeight())
//					img.setRGB(imgX, imgY, argb);
//			}
//		}
//		currentX += width;
//		if (currentX > targetSize.width) {
//			currentX = 0;
//			currentY += height;
//		}
//		if (currentY > targetSize.height)
//			currentX = currentY = 0;
//
//	}

	// public void export(final Image img) {
	// export(img.getData(0), img.getWidth(), img.getHeight());
	// }

	// public void export(final int[] data, final int width, final int height) {
	// final BufferedImage img = new BufferedImage(width, height, _useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
	// images.add(img);
	//
	// int index, r, g, b, a;
	// int argb;
	// for (int x = 0; x < width; x++) {
	// for (int y = 0; y < height; y++) {
	// index = (_useAlpha ? 4 : 3) * ((height - y - 1) * width + x);
	// r = data[index + 0];
	// g = data[index + 1];
	// b = data[index + 2];
	//
	// argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
	//
	// if (_useAlpha) {
	// a = data[index + 3];
	// argb |= (a & 0xFF) << 24;
	// }
	// img.setRGB(x, y, argb);
	// }
	// }
	// }

//	@Override
	public ImageDataFormat getFormat() {
		// if (_useAlpha) {
		// return ImageDataFormat.RGBA;
		// } else {
		return ImageDataFormat.RGB;
		// }
	}

	// public boolean isUseAlpha() {
	// return _useAlpha;
	// }
	//
	// public void setUseAlpha(final boolean useAlpha) {
	// _useAlpha = useAlpha;
	// }

	// @Override
	// public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
	// if (pageIndex >= images.size()) {
	// return NO_SUCH_PAGE;
	// }
	// final BufferedImage img = images.get(pageIndex);
	// /* The img is twice as big as the page so need to draw it based on paper size not img size */
	// graphics.drawImage(img, 0, 0, (int) pageFormat.getWidth(), (int) pageFormat.getHeight(), null);
	// // graphics.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
	// return PAGE_EXISTS;
	// }

	@Override
	public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
		if (pageIndex >= getNumberOfPages()) {
			return NO_SUCH_PAGE;
		}

		/* The img is twice as big as the page so need to draw it based on paper size not img size */
		if (lastImagePage != pageIndex) {
			getScreenShot(SceneManager.getInstance().getCanvas().getCanvasRenderer().getRenderer(), printCorners.get(pageIndex));
			lastImagePage  = pageIndex;
		}
		graphics.drawImage(image, 0, 0, (int) pageFormat.getWidth(), (int) pageFormat.getHeight(), null);
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
		// return images.size();
		return printCorners.size();
	}

	private synchronized BufferedImage getScreenShot(final Renderer renderer, final ReadOnlyVector3 printCorner) {
		final Camera camera = Camera.getCurrentCamera();
		final int width = camera.getWidth(), height = camera.getHeight();


		final Vector3 cameraLocation = new Vector3(printCorner).addLocal(visibleSceneWidth / 2.0, -10.0, -visibleSceneHeight / 2.0);
		final double pageToX = printCorner.getX() + PrintController.getInstance().getPageWidth() + visibleSceneWidth / 2.0;
		final double pageToZ = printCorner.getZ() - PrintController.getInstance().getPageHeight() - visibleSceneHeight / 2.0;

		int currentX = 0;
		int currentY = 0;

		int i = 0;
		while (true) {
//			pos.set(pos.getX() + x + w / 2.0, -10.0, pos.getZ() - y - h / 2.0);
			camera.setLocation(cameraLocation);
			camera.lookAt(cameraLocation.add(0, 1, 0, null), Vector3.UNIT_Z);
			SceneManager.getInstance().getCameraNode().updateFromCamera();


			SceneManager.getInstance().frameHandler.updateFrame();
			SceneManager.getInstance().frameHandler.updateFrame();

//			try {
//				Thread.sleep(1000);
//			} catch (final InterruptedException e) {
//				e.printStackTrace();
//			}
			// Ask the renderer for the current scene to be stored in the buffer
			_scratch = BufferUtils.createByteBuffer(camera.getWidth() * camera.getHeight() * ImageUtils.getPixelByteSize(getFormat(), ImageDataType.UnsignedByte));
			SceneManager.getInstance().getCanvas().getCanvasRenderer().makeCurrentContext();
			renderer.grabScreenContents(_scratch, getFormat(), 0, 0, width, height);
			SceneManager.getInstance().getCanvas().getCanvasRenderer().releaseCurrentContext();



			int index, r, g, b;
			int argb;
//			if (i == 0)
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					index = 3 * ((height - y - 1) * width + x);
					r = _scratch.get(index + 0);
					g = _scratch.get(index + 1);
					b = _scratch.get(index + 2);

					argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

					final int imgX = currentX + x;
					final int imgY = currentY + y;
//					System.out.println(imgX + "," + imgY + "\t");
					if (imgX < image.getWidth() && imgY < image.getHeight())
						image.setRGB(imgX, imgY, argb);
				}
			}

//			System.out.println("\n");

			currentX += width;
			if (currentX > targetSize.width) {
				currentX = 0;
				currentY += height;
			}
//			if (currentY > targetSize.height) {
//				currentX = currentY = 0;
//				break;
//			}

			// if (x + w < getPageWidth())
			// x += w;
			// else if (y + h < getPageHeight())
			// y += h;
			// else
			// break;

			// send the buffer to the exportable object for processing.
			// exportable.export(_scratch, width, height);

			if (cameraLocation.getX() + visibleSceneWidth < pageToX)
				cameraLocation.addLocal(visibleSceneWidth, 0, 0);
			else if (cameraLocation.getZ() - visibleSceneHeight > pageToZ) {
				cameraLocation.setX(printCorner.getX() + visibleSceneWidth / 2.0);
				cameraLocation.addLocal(0, 0, -visibleSceneHeight);
			} else
				break;

			i++;
			System.out.println(i);
//			if (i == 4)
//				break;
		}

		return image;
	}

}
