package org.concord.energy3d.scene;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Annotation;
import org.concord.energy3d.util.ObjectCloner;
import org.concord.energy3d.util.Printout;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.Updater;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;

public class PrintController implements Updater {
	private static PrintController instance = new PrintController();
	private static final double SPACE_BETWEEN_PAGES = 0.5;
	private static final double exactFromPageToWorldCoord = 1.0 / 72.0 / 4.0 / 10.6 * 10.8;
	private static double spaceBetweenParts = 0; // 0.3; // 0.5;
	private final ArrayList<ReadOnlyVector3> printCenters = new ArrayList<ReadOnlyVector3>();
	private final Timer timer = new Timer();
	private double pageWidth, pageHeight, pageLeft, pageRight, pageTop, pageBottom;
	private double angle;
	private int cols;
	private int rows;
	private boolean isPrintPreview = false;
	private boolean init = false;
	private boolean finish = false;
	private boolean finished = true;
	private boolean shadingSelected;
	private boolean shadowSelected;
	private ArrayList<HousePart> printParts;
	private final Node pagesRoot = new Node();
	private boolean heliodonSelected;
	private PageFormat pageFormat = new PageFormat();
	private boolean isScaleToFit;
	private boolean restartFlag = false;
	private double labelHeight = 0.0;

	public static PrintController getInstance() {
		return instance;
	}

	public PrintController() {
		final Paper paper = new Paper();
		paper.setSize(13 * 72, 19 * 72);
		final int m = (int) (0.25 * 72);
		paper.setImageableArea(m, m, paper.getWidth() - m * 2, paper.getHeight() - m * 2);
		pageFormat.setPaper(paper);

	}

	@Override
	public void init() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(final ReadOnlyTimer globalTimer) {
		if (isPrintPreview)
			rotate();

		if (isFinished())
			return;

		final Spatial originalHouseRoot = Scene.getInstance().getOriginalHouseRoot();
		if (init) {
			init = false;
			finish = false;
			if (!isPrintPreview) {
				Scene.getRoot().detachChild(pagesRoot);
				pagesRoot.detachAllChildren();
				for (final HousePart part : printParts)
					if (part instanceof Wall)
						((Wall) part).setBackMeshesVisible(true);

				for (final HousePart part : printParts) {
					part.hideLabels();
					part.getOriginal().hideLabels();
				}
			} else {
				printParts = (ArrayList<HousePart>) ObjectCloner.deepCopy(Scene.getInstance().getParts());
				for (int i = 0; i < printParts.size(); i++) {
					Scene.getRoot().attachChild(printParts.get(i).getRoot());
					printParts.get(i).setOriginal(Scene.getInstance().getParts().get(i));
				}

				for (final HousePart part : printParts)
					if (part.isPrintable()) {
						part.flatten(1.0);
						part.computeOrientedBoundingBox();
					}

				final ArrayList<ArrayList<Spatial>> pages = new ArrayList<ArrayList<Spatial>>();
				computePageDimension();
				computePrintCenters(pages);

				arrangePrintPages(pages);
				SceneManager.getInstance().updatePrintPreviewScene(true);
				drawPrintParts(0);
			}
			originalHouseRoot.getSceneHints().setCullHint(CullHint.Always);
			timer.reset();
		}

		final double viewSwitchDelay = 0.5;
		if (!finish && (!isPrintPreview || timer.getTimeInSeconds() > viewSwitchDelay)) {
			final double t = timer.getTimeInSeconds() - (isPrintPreview ? viewSwitchDelay : 0);
			drawPrintParts(isPrintPreview ? t : 1 - t);

			finish = t > 1;
			if (finish)
				timer.reset();
		}

		if (finish) {
			if (isPrintPreview)
				Scene.getRoot().attachChild(pagesRoot);
			final boolean doTheEndAnimation = timer.getTimeInSeconds() > viewSwitchDelay; // (time - startTime) > 1.0;
			if (!isPrintPreview && doTheEndAnimation) {
				originalHouseRoot.setRotation(new Matrix3().fromAngles(0, 0, 0));
				angle = 0;
				for (final HousePart housePart : printParts)
					Scene.getRoot().detachChild(housePart.getRoot());
				printParts = null;
				if (!isPrintPreview && restartFlag) {
					restartFlag = false;
//					/* to force redraw when animated back to normal scene */
//					Scene.getInstance().redrawAll();
//					Scene.getInstance().update();
					setPrintPreview(true);
					return;
				}
				originalHouseRoot.setScale(1);
				originalHouseRoot.setTranslation(0, 0, 0);
				originalHouseRoot.updateGeometricState(timer.getTimePerFrame(), true);

				final CanvasRenderer renderer = SceneManager.getInstance().getCanvas().getCanvasRenderer();
				renderer.makeCurrentContext();
				renderer.getRenderer().setBackgroundColor(ColorRGBA.BLACK);
				renderer.releaseCurrentContext();

				SceneManager.getInstance().setShading(shadingSelected);
				SceneManager.getInstance().setShadow(shadowSelected);
				SceneManager.getInstance().getHeliodon().setVisible(heliodonSelected);

				SceneManager.getInstance().updatePrintPreviewScene(false);
				if (!doTheEndAnimation) { // to avoid concurrency exception
					setFinished(true);
				}
			}

			if (printParts != null)
				for (final HousePart part : printParts)
					if (part instanceof Foundation)
						part.getRoot().getSceneHints().setCullHint(isPrintPreview ? CullHint.Always : CullHint.Inherit);

			if (isPrintPreview) {
				for (final HousePart part : printParts)
					if (part instanceof Wall)
						((Wall) part).setBackMeshesVisible(false);
			}

			if (isPrintPreview || doTheEndAnimation) {
				originalHouseRoot.getSceneHints().setCullHint(CullHint.Inherit);
				if (isPrintPreview) {
					int printSequence = 0;
					for (final HousePart part : printParts) {
						part.getOriginal().drawLabels(printSequence);
						printSequence = part.drawLabels(printSequence);
					}
					SceneManager.getInstance().update();
				}
				setFinished(true);
			}
		}
	}

	private void drawPrintParts(double flattenTime) {
		if (printParts == null)
			return;
		if (flattenTime < 0)
			flattenTime = 0;
		if (flattenTime > 1)
			flattenTime = 1;

		for (final HousePart part : printParts) {
			if (part.isPrintable()) {
				part.flatten(flattenTime);
			} else if (part instanceof Window) {
				((Window) part).hideBars();
			}
		}
	}

	public void print() {
//		SceneManager.getTaskManager().update(new Callable<Object>() {
//			@Override
//			public Object call() throws Exception {
				Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Always);
				final Component canvas = (java.awt.Component) SceneManager.getInstance().getCanvas();
				final int resolutionHeight = 2;
				final Dimension newSize;
//				if (Config.isMac())
//					newSize = new Dimension((int) (canvas.getHeight() * pageWidth / pageHeight), canvas.getHeight());
//				else
					newSize = new Dimension(resolutionHeight * (int) pageFormat.getWidth(), resolutionHeight * (int) pageFormat.getHeight());


//				canvas.setSize(newSize);
//				canvas.setPreferredSize(newSize);
//				canvas.invalidate();

//				if (!Config.isMac())
//					canvas.setSize(new Dimension(resolutionHeight * (int) pageFormat.getWidth(), resolutionHeight * (int) pageFormat.getHeight()));



				SceneManager.getInstance().resetCamera(ViewMode.PRINT);

				final Dimension orgCanvasSize = canvas.getSize();
				final Dimension canvasSize = (Dimension) orgCanvasSize.clone();
//				System.out.println(canvasSize);
				if (canvasSize.width % 32 != 0) {
					canvasSize.width -= canvasSize.width % 32;
					canvas.setSize(canvasSize);
					canvas.validate();
				}
				final double ratio = (double) canvasSize.width / canvasSize.height;
				final double cols = newSize.getWidth() / canvasSize.getWidth();
				final double rows = newSize.getHeight() / canvasSize.getHeight();
				final double pageWidth = PrintController.getInstance().getPageWidth() / cols;
				final double pageHeight = PrintController.getInstance().getPageHeight() / rows;

//				System.out.println("CANVAS SIZE ========== ");
//				System.out.println(canvasSize);
//				System.out.println(rows + "\t" + cols);
//				System.out.println("PageWidth segment: " + pageWidth);
//				System.out.println("PageHeight segment: " + pageHeight);

				if (ratio > pageWidth / pageHeight)
					SceneManager.getInstance().resizeCamera(pageHeight * ratio);
				else
					SceneManager.getInstance().resizeCamera(pageWidth);


				SceneManager.getInstance().update();
				final Printout printout = new Printout(pageFormat, newSize, pageWidth, pageHeight, printCenters);
				print(0, printout, 0, 0, pageWidth, pageHeight);;

				Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Inherit);
//				MainPanel.getInstance().validate();

				canvas.setSize(orgCanvasSize);
				canvas.validate();

				SceneManager.getInstance().resetCamera(ViewMode.PRINT_PREVIEW);
				SceneManager.getInstance().update();
//				return null;
//			}
//		});
	}

//	private void print(final int pageNum, final Printout printout, final double x, final double y, final double w, final double h) {
//		SceneManager.getTaskManager().render(new Callable<Object>() {
//			@Override
//			public Object call() throws Exception {
//				if (pageNum == printCenters.size() + 1) {
//					final PrinterJob job = PrinterJob.getPrinterJob();
//					job.setPageable(printout);
//					SwingUtilities.invokeLater(new Runnable() {
//						@Override
//						public void run() {
//							Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Inherit);
//							MainPanel.getInstance().validate();
//
//							SceneManager.getInstance().resetCamera(ViewMode.PRINT_PREVIEW);
//
//							SceneManager.getTaskManager().render(new Callable<Object>() {
//								@Override
//								public Object call() throws Exception {
//									SceneManager.getTaskManager().render(new Callable<Object>() {
//										@Override
//										public Object call() throws Exception {
//											if (job.printDialog())
//												try {
//													job.print();
//												} catch (final PrinterException exc) {
//													exc.printStackTrace();
//												}
//											return null;
//										}
//									});
//									return null;
//								}
//							});
//						}
//					});
//				} else {
//					if (pageNum != 0 || x != 0 || y != 0) {
////						Thread.sleep(1000);
//						ScreenExporter.exportCurrentScreen(SceneManager.getInstance().getCanvas().getCanvasRenderer().getRenderer(), printout);
//					}
//
//					if (pageNum < printCenters.size()) {
//						final Vector3 pos = new Vector3(printCenters.get(pageNum));
//						pos.set(pos.getX() + x + w / 2.0, -10.0, pos.getZ() - y - h / 2.0);
//						final Camera camera = Camera.getCurrentCamera();
//						camera.setLocation(pos);
//						camera.lookAt(pos.add(0, 1, 0, null), Vector3.UNIT_Z);
//						SceneManager.getInstance().getCameraNode().updateFromCamera();
//						if (x + w < getPageWidth())
//							print(pageNum, printout, x + w, y, w, h);
//						else if (y + h < getPageHeight())
//							print(pageNum, printout, 0, y + h, w, h);
//						else
//							print(pageNum + 1, printout, 0, 0, w, h);
////						print(pageNum + 1, printout, x, y);
////						print(pageNum, printout, x, y, w, h);
//					} else
//						print(pageNum + 1, printout, 0, 0, w, h);
//
//				}
//				return null;
//			}
//		});
//	}

	private void print(final int pageNum, final Printout printout, final double x, final double y, final double w, final double h) {
					final PrinterJob job = PrinterJob.getPrinterJob();
					job.setPageable(printout);
											if (job.printDialog())
												try {
													job.print();
												} catch (final PrinterException exc) {
													exc.printStackTrace();
												}
	}

	public void setPrintPreview(final boolean printPreview) {
		if (printPreview == isPrintPreview)
			return;
		isPrintPreview = printPreview;
		init = true;
		setFinished(false);
		if (printPreview) {
			shadingSelected = SceneManager.getInstance().isShadingEnabled();
			shadowSelected = SceneManager.getInstance().isShadowEnabled();
			heliodonSelected = SceneManager.getInstance().isHeliodonControlEnabled();
			if (shadingSelected)
				SceneManager.getInstance().setShading(false);
			if (shadowSelected)
				SceneManager.getInstance().setShadow(false);
			if (heliodonSelected)
				SceneManager.getInstance().getHeliodon().setVisible(false);
		}
	}

	public boolean isPrintPreview() {
		return isPrintPreview;
	}

	public void rotate() {
		if (SceneManager.getInstance().isRotationAnimationOn()) {
			angle += 0.01;
			Scene.getInstance().getOriginalHouseRoot().setRotation(new Matrix3().fromAngles(0, 0, angle));
		}
	}

	public ArrayList<HousePart> getPrintParts() {
		return printParts;
	}

	private void setFinished(final boolean finished) {
		this.finished = finished;
		MainPanel.getInstance().getPreviewButton().setEnabled(finished);
		MainFrame.getInstance().getPreviewMenuItem().setEnabled(finished);
		if (isPrintPreview() || finished)
			MainPanel.getInstance().setToolbarEnabled(!isPrintPreview());
	}

	public boolean isFinished() {
		return finished;
	}

	private void computePageDimension() {
		spaceBetweenParts = Scene.isAnnotationsVisible() ? 0.3 : 0;
		final double fromPageToWorldCoord;
		if (!isScaleToFit) {
			fromPageToWorldCoord = exactFromPageToWorldCoord;
		} else {
			double maxWidth = 0;
			double maxHeight = 0;
			for (final HousePart printPart : printParts) {
				if (printPart.isPrintable()) {
					if (printPart instanceof Roof) {
						for (final Spatial roofPartNode : ((Roof) printPart).getRoofPartsRoot().getChildren()) {
							if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
								final OrientedBoundingBox boundingBox = (OrientedBoundingBox) ((Node) roofPartNode).getChild(0).getWorldBound();
								final double width = Math.min(boundingBox.getExtent().getX(), boundingBox.getExtent().getZ());
								final double height = Math.max(boundingBox.getExtent().getX(), boundingBox.getExtent().getZ());
								if (width > maxWidth)
									maxWidth = width;
								if (height > maxHeight)
									maxHeight = height;
							}
						}
					} else {
						final OrientedBoundingBox boundingBox = (OrientedBoundingBox) printPart.getMesh().getWorldBound();
						final double width = Math.min(boundingBox.getExtent().getX(), boundingBox.getExtent().getZ());
						final double height = Math.max(boundingBox.getExtent().getX(), boundingBox.getExtent().getZ());
						if (width > maxWidth)
							maxWidth = width;
						if (height > maxHeight)
							maxHeight = height;

					}
				}
			}

			maxWidth *= 2;
			maxHeight *= 2;

			maxWidth += spaceBetweenParts;
			maxHeight += spaceBetweenParts;
			final double ratio = pageFormat.getImageableWidth() / pageFormat.getImageableHeight();
			if (maxWidth / maxHeight > ratio) {
				pageWidth = ratio < 1 ? Math.min(maxWidth, maxHeight) : Math.max(maxWidth, maxHeight);
				pageHeight = pageWidth / ratio;
			} else {
				pageHeight = ratio < 1 ? Math.max(maxWidth, maxHeight) : Math.min(maxWidth, maxHeight);
				pageWidth = pageHeight * ratio;
			}
			fromPageToWorldCoord = pageWidth / pageFormat.getImageableWidth();
		}

		pageLeft = pageFormat.getImageableX() * fromPageToWorldCoord + spaceBetweenParts / 2.0;
		pageRight = (pageFormat.getImageableX() + pageFormat.getImageableWidth()) * fromPageToWorldCoord - spaceBetweenParts / 2.0;
		pageTop = pageFormat.getImageableY() * fromPageToWorldCoord + spaceBetweenParts / 2.0;
		if (labelHeight == 0.0) {
			final BMText label = Annotation.makeNewLabel();
			label.setFontScale(0.05);
			labelHeight = label.getHeight();
		}
		pageBottom = (pageFormat.getImageableY() + pageFormat.getImageableHeight()) * fromPageToWorldCoord - spaceBetweenParts / 2.0 - labelHeight;

		pageWidth = pageFormat.getWidth() * fromPageToWorldCoord;
		pageHeight = pageFormat.getHeight() * fromPageToWorldCoord;
	}

	private void arrangePrintPages(final ArrayList<ArrayList<Spatial>> pages) {
		final double ratio = (double) Camera.getCurrentCamera().getWidth() / Camera.getCurrentCamera().getHeight();
		cols = (int) Math.round(Math.sqrt(pages.size() + 4) * ratio);
		if (cols % 2 == 0)
			cols++;
		rows = (int) Math.ceil((pages.size() + 4) / cols);

		int pageNum = 0;
		printCenters.clear();
		for (final ArrayList<Spatial> page : pages) {
			final Vector3 upperLeftCorner = new Vector3();
			double x, z;
			final BoundingBox boundingBox = (BoundingBox) new BoundingBox().merge(Scene.getInstance().getOriginalHouseRoot().getWorldBound());
			final double minXDistance = boundingBox.getXExtent() + pageWidth / 2.0;
			final double minYDistance = boundingBox.getZExtent();
			do {
				x = (pageNum % cols - cols / 2) * (pageWidth + SPACE_BETWEEN_PAGES) + boundingBox.getCenter().getX();
				z = (pageNum / cols) * (pageHeight + SPACE_BETWEEN_PAGES);
				upperLeftCorner.setX(x - pageWidth / 2.0);
				upperLeftCorner.setZ(z + pageHeight);
				upperLeftCorner.setY(boundingBox.getCenter().getY());
				pageNum++;
			} while (Math.abs(x - boundingBox.getCenter().getX()) < minXDistance && Math.abs(z - boundingBox.getCenter().getZ()) < minYDistance);

//			printCenters.add(new Vector3(x, 0, z + pageHeight / (Config.isMac() ? 1.0 : 2.0)));
			printCenters.add(upperLeftCorner);

			for (final Spatial printSpatial : page)
				((UserData) printSpatial.getUserData()).getPrintCenter().addLocal(upperLeftCorner);

			final Box box = new Box("Page Boundary");
			box.setData(upperLeftCorner.add(0, 0.1, 0, null), upperLeftCorner.add(pageWidth, 0.2, -pageHeight, null));
			pagesRoot.attachChild(box);

			final BMText footNote = Annotation.makeNewLabel();
			final String url = Scene.getURL() != null ? Scene.getURL().getFile().substring(Scene.getURL().getFile().lastIndexOf('/') + 1, Scene.getURL().getFile().length()) + " -" : "";
			footNote.setText(url.replaceAll("%20", " ") + " Page " + printCenters.size() + " / " + pages.size() + " - http://energy.concord.org/");
			footNote.setFontScale(0.05);
			footNote.setAlign(Align.North);
			footNote.setTranslation(upperLeftCorner.add(pageWidth / 2.0, 0.0, -pageBottom - spaceBetweenParts / 2.0, null));
			pagesRoot.attachChild(footNote);
		}
	}

	private void computePrintCenters(final ArrayList<ArrayList<Spatial>> pages) {
		for (final HousePart printPart : printParts) {
			if (printPart.isPrintable()) {
				printPart.getRoot().updateWorldTransform(true);
				printPart.getRoot().updateWorldBound(true);
				if (printPart instanceof Roof) {
					final Roof roof = (Roof) printPart;
					for (final Spatial roofPart : roof.getRoofPartsRoot().getChildren())
						if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
							final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
							roof.setPrintVertical(roofPart, decideVertical(mesh));
							computePrintCenterOf(mesh, pages);
						}
				} else {
					final Mesh mesh = printPart.getMesh();
					printPart.setPrintVertical(decideVertical(mesh));
					computePrintCenterOf(mesh, pages);
				}
			}
		}
	}

	private boolean decideVertical(final Mesh mesh) {
		final OrientedBoundingBox bound = (OrientedBoundingBox) mesh.getWorldBound();
		final boolean isMeshVertical = bound.getExtent().getX() < bound.getExtent().getZ();
		final double imageableWidth = pageRight - pageLeft;
		return isMeshVertical && bound.getExtent().getZ() * 2 < imageableWidth || !isMeshVertical && bound.getExtent().getX() * 2 > imageableWidth;
	}

	private void computePrintCenterOf(final Spatial printPart, final ArrayList<ArrayList<Spatial>> pages) {
		boolean isFitted = false;
		for (int pageNum = 0; pageNum < pages.size() && !isFitted; pageNum++)
			isFitted = fitInPage(printPart, pages.get(pageNum));
		if (!isFitted) {
			printPart.updateWorldBound(true);
			final OrientedBoundingBox bounds = (OrientedBoundingBox) printPart.getWorldBound();
			((UserData) printPart.getUserData()).setPrintCenter(new Vector3(bounds.getExtent().getX() + pageLeft, 0, -bounds.getExtent().getZ() - pageTop));
			final ArrayList<Spatial> page = new ArrayList<Spatial>();
			page.add(printPart);
			pages.add(page);
		}
	}

	private boolean fitInPage(final Spatial printPart, final ArrayList<Spatial> page) {
		for (final Spatial neighborPart : page) {
			final Vector3 neighborPartCenter = ((UserData) neighborPart.getUserData()).getPrintCenter();
			final OrientedBoundingBox neighborBound = (OrientedBoundingBox) neighborPart.getWorldBound().clone(null);
			final OrientedBoundingBox printPartBound = (OrientedBoundingBox) printPart.getWorldBound().clone(null);
			final double xExtend = neighborBound.getExtent().getX() + printPartBound.getExtent().getX() + spaceBetweenParts;
			final double zExtend = neighborBound.getExtent().getZ() + printPartBound.getExtent().getZ() + spaceBetweenParts;

			for (double angleQuarter = 0; angleQuarter < 4; angleQuarter++) {
				final boolean isHorizontal = angleQuarter % 2 == 0;
				final Vector3 tryCenter = new Matrix3().fromAngles(0, angleQuarter * Math.PI / 2.0, 0).applyPost(new Vector3(isHorizontal ? xExtend : zExtend, 0, 0), null);
				tryCenter.addLocal(neighborPartCenter);
				if (!isHorizontal)
					tryCenter.setX(pageLeft + printPartBound.getExtent().getX());

				if (!isHorizontal)
					tryCenter.setX(MathUtils.clamp(tryCenter.getX(), pageLeft + printPartBound.getExtent().getX(), pageRight - printPartBound.getExtent().getX()));
				else
					tryCenter.setZ(MathUtils.clamp(tryCenter.getZ(), -pageBottom + printPartBound.getExtent().getZ(), -pageTop - printPartBound.getExtent().getZ()));

				tryCenter.setY(0);

				boolean collision = false;
				if (tryCenter.getX() - printPartBound.getExtent().getX() < pageLeft - MathUtils.ZERO_TOLERANCE || tryCenter.getX() + printPartBound.getExtent().getX() > pageRight + MathUtils.ZERO_TOLERANCE || tryCenter.getZ() + printPartBound.getExtent().getZ() > -pageTop + MathUtils.ZERO_TOLERANCE || tryCenter.getZ() - printPartBound.getExtent().getZ() < -pageBottom - MathUtils.ZERO_TOLERANCE)
					collision = true;
				else
					for (final Spatial otherPart : page) {
						printPartBound.setCenter(tryCenter);
						final OrientedBoundingBox otherPartBound = (OrientedBoundingBox) otherPart.getWorldBound().clone(null);
						otherPartBound.setCenter(((UserData) otherPart.getUserData()).getPrintCenter());

						if (printPartBound.getExtent().getX() + otherPartBound.getExtent().getX() > Math.abs(printPartBound.getCenter().getX() - otherPartBound.getCenter().getX()) + MathUtils.ZERO_TOLERANCE && printPartBound.getExtent().getZ() + otherPartBound.getExtent().getZ() > Math.abs(printPartBound.getCenter().getZ() - otherPartBound.getCenter().getZ()) + MathUtils.ZERO_TOLERANCE) {
							collision = true;
							break;
						}
					}
				if (!collision) {
					((UserData) printPart.getUserData()).setPrintCenter(tryCenter);
					page.add(printPart);
					return true;
				}
			}
		}
		return false;
	}

	public double getPageWidth() {
		return pageWidth;
	}

	public double getPageHeight() {
		return pageHeight;
	}

	public ReadOnlyVector3 getZoomAllCameraLocation() {
		final double pageHeight = getPageHeight() + SPACE_BETWEEN_PAGES;
		final double w = cols * (getPageWidth() + SPACE_BETWEEN_PAGES);
		final double h = rows * pageHeight;
		return Scene.getInstance().getOriginalHouseRoot().getWorldBound().getCenter().add(0, -Math.max(w, h), h / 2, null);
	}

	public void pageSetup() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final PageFormat pf = PrinterJob.getPrinterJob().pageDialog(pageFormat);
				if (pf != pageFormat) {
					pageFormat = pf;
					if (isPrintPreview())
						restartAnimation();
				}
			}
		});
	}

	public void setScaleToFit(final boolean scaleToFit) {
		isScaleToFit = scaleToFit;
		if (isPrintPreview())
			restartAnimation();
	}

	public void restartAnimation() {
		restartFlag = true;
		setPrintPreview(false);
	}
}
