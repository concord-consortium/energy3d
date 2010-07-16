package org.concord.energy3d.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.util.screen.ScreenExportable;

public class PrintExporter implements ScreenExportable, Printable {
	protected boolean _useAlpha;
//	private BufferedImage[] images;
	private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
//	private int currentPage = 0;

	public PrintExporter() {
		this(false);
	}

	public PrintExporter(boolean useAlpha) {
		_useAlpha = useAlpha;
//		images = new BufferedImage[pages];

	}

	public void export(final ByteBuffer data, final int width, final int height) {
//		images[currentPage] = new BufferedImage(width, height, _useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		final BufferedImage img = new BufferedImage(width, height, _useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		images.add(img);

		int index, r, g, b, a;
		int argb;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				index = (_useAlpha ? 4 : 3) * ((height - y - 1) * width + x);
				r = ((data.get(index + 0)));
				g = ((data.get(index + 1)));
				b = ((data.get(index + 2)));

				argb = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

				if (_useAlpha) {
					a = ((data.get(index + 3)));
					argb |= (a & 0xFF) << 24;
				}

//				images[currentPage].setRGB(x, y, argb);
				img.setRGB(x, y, argb);
			}
		}
//		currentPage++;
		// print(img);

	}

	// private void print(BufferedImage img) {
	// PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
	// pras.add(new Copies(1));
	// PrintService pss[] = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.GIF, pras);
	// if (pss.length == 0)
	// throw new RuntimeException("No printer services available.");
	// PrintService ps = pss[0];
	// System.out.println("Printing to " + ps);
	// DocPrintJob job = ps.createPrintJob();
	// job.setPrintable(new PrintInterface(img));
	// FileInputStream fin = new FileInputStream("YOurImageFileName.PNG");
	// Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.GIF, null);
	// job.print(doc, pras);
	// fin.close();
	// }

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

		Graphics2D g2d = (Graphics2D) graphics;
		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

//		graphics.drawImage(images[pageIndex], 0, 0, 600, 600 * images[pageIndex].getHeight() / images[pageIndex].getWidth() , null);
		final BufferedImage img = images.get(pageIndex);
		graphics.drawImage(img, 0, 0, 600, 600 * img.getHeight() / img.getWidth() , null);

		// AffineTransform at = AffineTransform.getScaleInstance((double) width / images[pageIndex].getWidth(), (double) height / images[pageIndex].getHeight());
		// graphics.drawImage(images[pageIndex], at);

//		return NO_SUCH_PAGE;
		return PAGE_EXISTS;
	}

//	public int getCurrentPage() {
//		return currentPage;
//	}
}
