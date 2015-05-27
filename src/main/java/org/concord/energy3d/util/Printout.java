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
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.util.geom.BufferUtils;

public class Printout implements Printable, Pageable {
	private final ArrayList<ReadOnlyVector3> printCorners;
	private final BufferedImage image;
	private final PageFormat pageFormat;
	private final Dimension targetSize;
	private final double visibleSceneWidth;
	private final double visibleSceneHeight;
	private final ByteBuffer _scratch;
	private int lastImagePage = -1;

	public Printout(final PageFormat pageFormat, final Dimension targetSize, final double visibleSceneWidth, final double visibleSceneHeight, final ArrayList<ReadOnlyVector3> printCorners) {
		this.pageFormat = pageFormat;
		this.targetSize = targetSize;
		this.visibleSceneWidth = visibleSceneWidth;
		this.visibleSceneHeight = visibleSceneHeight;
		this.printCorners = printCorners;
		final Camera camera = Camera.getCurrentCamera();
		_scratch = BufferUtils.createByteBuffer(camera.getWidth() * camera.getHeight() * ImageUtils.getPixelByteSize(getFormat(), PixelDataType.UnsignedByte));
		image = new BufferedImage(targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB);
	}

	public static ImageDataFormat getFormat() {
		return ImageDataFormat.RGB;
	}

	@Override
	public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
		if (pageIndex >= getNumberOfPages()) {
			return NO_SUCH_PAGE;
		}

		/* The img is twice as big as the page so need to draw it based on paper size not img size */
		if (lastImagePage != pageIndex) {
			getScreenShot(SceneManager.getInstance().getCanvas().getCanvasRenderer().getRenderer(), printCorners.get(pageIndex));
			lastImagePage = pageIndex;
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
		return printCorners.size();
	}

	private synchronized BufferedImage getScreenShot(final Renderer renderer, final ReadOnlyVector3 printCorner) {
		final Camera camera = Camera.getCurrentCamera();
		final int width = camera.getWidth(), height = camera.getHeight();

		final Vector3 cameraLocation = new Vector3(printCorner).addLocal(visibleSceneWidth / 2.0, Scene.getOriginalHouseRoot().getWorldBound().getCenter().getY() - 10, -visibleSceneHeight / 2.0);
		final double pageToX = printCorner.getX() + PrintController.getInstance().getPageWidth() + visibleSceneWidth / 2.0;
		final double pageToZ = printCorner.getZ() - PrintController.getInstance().getPageHeight() - visibleSceneHeight / 2.0;

		int currentX = 0;
		int currentY = 0;

		while (true) {
			camera.setLocation(cameraLocation);
			camera.lookAt(cameraLocation.add(0, 1, 0, null), Vector3.UNIT_Z);

			SceneManager.getInstance().getFrameHandler().updateFrame();
			SceneManager.getInstance().getFrameHandler().updateFrame();

			// Ask the renderer for the current scene to be stored in the buffer
			takeSnapShot(image, _scratch, renderer, width, height, currentX, currentY);

			currentX += width;
			if (currentX > targetSize.width) {
				currentX = 0;
				currentY += height;
			}

			if (cameraLocation.getX() + visibleSceneWidth < pageToX)
				cameraLocation.addLocal(visibleSceneWidth, 0, 0);
			else if (cameraLocation.getZ() - visibleSceneHeight > pageToZ) {
				cameraLocation.setX(printCorner.getX() + visibleSceneWidth / 2.0);
				cameraLocation.addLocal(0, 0, -visibleSceneHeight);
			} else
				break;
		}

		return image;
	}

	public static BufferedImage takeSnapShot() {
		final Camera camera = Camera.getCurrentCamera();
		final int width = camera.getWidth() - camera.getWidth() % 4;
		final int height = camera.getHeight();
		final ByteBuffer _scratch = BufferUtils.createByteBuffer(width * height * ImageUtils.getPixelByteSize(getFormat(), PixelDataType.UnsignedByte));
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		takeSnapShot(image, _scratch, SceneManager.getInstance().getCanvas().getCanvasRenderer().getRenderer(), width, height, 0, 0);
		return image;
	}

	private static void takeSnapShot(final BufferedImage image, final ByteBuffer _scratch, final Renderer renderer, final int width, final int height, final int currentX, final int currentY) {
		SceneManager.getInstance().getCanvas().getCanvasRenderer().makeCurrentContext();
		renderer.grabScreenContents(_scratch, getFormat(), 0, 0, width, height);
		SceneManager.getInstance().getCanvas().getCanvasRenderer().releaseCurrentContext();

		int index, r, g, b;
		int argb;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				index = 3 * ((height - y - 1) * width + x);
				r = _scratch.get(index + 0);
				g = _scratch.get(index + 1);
				b = _scratch.get(index + 2);

				argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

				final int imgX = currentX + x;
				final int imgY = currentY + y;
				if (imgX < image.getWidth() && imgY < image.getHeight())
					image.setRGB(imgX, imgY, argb);
			}
		}
	}

}
