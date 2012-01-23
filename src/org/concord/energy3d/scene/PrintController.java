package org.concord.energy3d.scene;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.util.ObjectCloner;
import org.concord.energy3d.util.Printout;
import org.concord.energy3d.util.Util;

import sun.misc.Sort;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
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
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.screen.ScreenExporter;

public class PrintController implements Updater {
	private static PrintController instance = new PrintController();
	private static final int SPACE_BETWEEN_PAGES = 5;
	private static final double PAGE_MARGIN = 0.2;
	private static final double SPACE_BETWEEN_PARTS = 0.3; // 0.5;
	private final ArrayList<Vector3> printCenters = new ArrayList<Vector3>();
	private final Timer timer = new Timer();
	private final Paper paper = new Paper();
	private double pageWidth, pageHeight;
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
	private Node pagesRoot = new Node();
	private boolean heliodonSelected;

	public static PrintController getInstance() {
		return instance;
	}

	public void init() {
	}

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
			} else {
				printParts = (ArrayList<HousePart>) ObjectCloner.deepCopy(Scene.getInstance().getParts());
				for (int i = 0; i < printParts.size(); i++) {
					Scene.getRoot().attachChild(printParts.get(i).getRoot());
					printParts.get(i).setOriginal(Scene.getInstance().getParts().get(i));
				}

				int printSequence = 0;
				for (final HousePart part : printParts)
					if (part.isPrintable()) {
//						part.flattenInit();
						part.flatten(1.0);
						part.computeOrientedBoundingBox();
						part.drawLabels(printSequence);
						printSequence = part.getOriginal().drawLabels(printSequence);
					}
				
				final ArrayList<ArrayList<Spatial>> pages = new ArrayList<ArrayList<Spatial>>();
				computePageDimension();
				computePrintCenters(pages);
				
//				finished = true;
//				if (true)
//					return;
				
				arrangePrintPages(pages);

				SceneManager.getInstance().updatePrintPreviewScene(true);

				originalHouseRoot.setScale(2);
				// originalHouseRoot.setTranslation(0, 0, -Util.findExactHeight(printParts));
				// originalHouseRoot.updateWorldTransform(false);
				// originalHouseRoot.updateWorldBound(false);
				originalHouseRoot.setTranslation(originalHouseRoot.getWorldBound().getCenter().multiply(-2.0, null));

				drawPrintParts(0);
			}
			originalHouseRoot.getSceneHints().setCullHint(CullHint.Always);
			timer.reset();
			System.out.println("Print Preview init done.");
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
				// Scene.getInstance().updateTextSizes();
				if (!doTheEndAnimation) // to avoid concurrency exception
					setFinished(true);
			}

			if (isPrintPreview || doTheEndAnimation) {
				int printSequence = 0;
				originalHouseRoot.getSceneHints().setCullHint(CullHint.Inherit);

				if (printParts != null)
					for (final HousePart part : printParts)
						if (part instanceof Foundation)
							part.getRoot().getSceneHints().setCullHint(isPrintPreview ? CullHint.Always : CullHint.Inherit);

				for (final HousePart part : Scene.getInstance().getParts()) {
					if (isPrintPreview)
						printSequence = part.drawLabels(printSequence);
					else
						part.hideLabels();
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
		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() throws Exception {
				Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Always);
				final Printout printout = new Printout(paper);
				final Component canvas = (java.awt.Component) SceneManager.getInstance().getCanvas();
				// final Paper paper = new Paper();
				final int resolutionHeight = 2;
				// final Dimension newSize = new Dimension((int) (resolutionHeight * paper.getWidth() / paper.getHeight()), resolutionHeight);
				final Dimension newSize = new Dimension((int) (resolutionHeight * paper.getWidth()), (int) (resolutionHeight * paper.getHeight()));
				canvas.setSize(newSize);
				SceneManager.getInstance().resetCamera(ViewMode.PRINT);
				print(0, printout);
				return null;
			}
		});
	}

	private void print(final int pageNum, final Printout printout) {
		SceneManager.getTaskManager().render(new Callable<Object>() {
			public Object call() throws Exception {
				// final int nextPage = pageNum + 1;
				if (pageNum == printCenters.size() + 1) {
					final PrinterJob job = PrinterJob.getPrinterJob();
					job.setPageable(printout);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Inherit);
							// MainFrame.getInstance().getMainPanel().validate();
							MainPanel.getInstance().validate();

							SceneManager.getInstance().resetCamera(ViewMode.PRINT_PREVIEW);

							SceneManager.getTaskManager().render(new Callable<Object>() {
								public Object call() throws Exception {
									SceneManager.getTaskManager().render(new Callable<Object>() {
										public Object call() throws Exception {
											if (job.printDialog())
												try {
													job.print();
												} catch (PrinterException exc) {
													exc.printStackTrace();
												}
											return null;
										}
									});
									return null;
								}
							});
						}
					});
				} else {
					if (pageNum != 0) {
						ScreenExporter.exportCurrentScreen(SceneManager.getInstance().getCanvas().getCanvasRenderer().getRenderer(), printout);
					}
					if (pageNum < printCenters.size()) {
						final Vector3 pos = printCenters.get(pageNum);
						final Camera camera = Camera.getCurrentCamera();
						camera.setLocation(pos.getX(), -10.0, pos.getZ());
						camera.lookAt(pos.add(0, 1, 0, null), Vector3.UNIT_Z);
						SceneManager.getInstance().getCameraNode().updateFromCamera();
					}
					print(pageNum + 1, printout);

				}
				// if (pageNum != 0) {
				// final CanvasRenderer canvasRenderer = SceneManager.getInstance().getCanvas().getCanvasRenderer();
				// Thread.sleep(1000);
				// ScreenExporter.exportCurrentScreen(canvasRenderer.getRenderer(), printout);
				// }
				// if (nextPage < printCenters.size()) {
				// final Vector3 pos = printCenters.get(nextPage);
				// final Camera camera = Camera.getCurrentCamera();
				// camera.setLocation(pos.getX(), -1.0, pos.getZ());
				// camera.lookAt(pos.add(0, 1, 0, null), Vector3.UNIT_Z);
				// SceneManager.getInstance().getCameraNode().updateFromCamera();
				// print(nextPage, printout);
				// } else {
				// final PrinterJob job = PrinterJob.getPrinterJob();
				// job.setPageable(printout);
				// final PrinterJob job = PrinterJob.getPrinterJob();
				// final PageFormat pageFormat = new PageFormat();
				// final Paper paper = new Paper();
				// paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
				// pageFormat.setPaper(paper);
				//
				// job.setPageable(new Pageable() {
				// @Override
				// public Printable getPrintable(int arg0) throws IndexOutOfBoundsException {
				// return printout;
				// }
				//
				// @Override
				// public PageFormat getPageFormat(int arg0) throws IndexOutOfBoundsException {
				// return pageFormat;
				// }
				//
				// @Override
				// public int getNumberOfPages() {
				// return printCenters.size();
				// }
				// });

				// SwingUtilities.invokeLater(new Runnable() {
				// @Override
				// public void run() {
				// Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Inherit);
				// MainFrame.getInstance().getMainPanel().validate();
				// SceneManager.getInstance().resetCamera(ViewMode.PRINT_PREVIEW);
				// SceneManager.getTaskManager().render(new Callable<Object>() {
				// public Object call() throws Exception {
				// SceneManager.getTaskManager().render(new Callable<Object>() {
				// public Object call() throws Exception {
				// if (job.printDialog())
				// try {
				// job.print();
				// } catch (PrinterException exc) {
				// exc.printStackTrace();
				// }
				// return null;
				// }
				// });
				// return null;
				// }
				// });
				// }
				// });
				// }
				return null;
			}
		});
	}

	public void setPrintPreview(final boolean printPreview) {
		if (printPreview == isPrintPreview)
			return;
		// while(!isFinished())
		// Thread.yield();
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
			// Scene.getInstance().updateTextSizes(0.1);
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
		double maxWidth = 0;
		double maxHeight = 0;
		for (final HousePart printPart : printParts) {
			if (printPart.isPrintable()) {
				if (printPart instanceof Roof) {
					for (final Spatial roofPartNode : ((Roof) printPart).getRoofPartsRoot().getChildren()) {
						if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
							// maxWidth = Math.max(maxWidth, ((BoundingBox) mesh.getWorldBound()).getXExtent() * 2);
							// maxHeight = Math.max(maxHeight, ((BoundingBox) mesh.getWorldBound()).getZExtent() * 2);
							maxWidth = Math.max(maxWidth, ((OrientedBoundingBox) ((Node) roofPartNode).getChild(0).getWorldBound()).getExtent().getX() * 2);
							maxHeight = Math.max(maxHeight, ((OrientedBoundingBox) ((Node) roofPartNode).getChild(0).getWorldBound()).getExtent().getZ() * 2);
						}
					}
				} else {
					// maxWidth = Math.max(maxWidth, ((BoundingBox) printPart.getMesh().getWorldBound()).getXExtent() * 2);
					// maxHeight = Math.max(maxHeight, ((BoundingBox) printPart.getMesh().getWorldBound()).getZExtent() * 2);
					maxWidth = Math.max(maxWidth, ((OrientedBoundingBox) printPart.getMesh().getWorldBound()).getExtent().getX() * 2);
					maxHeight = Math.max(maxHeight, ((OrientedBoundingBox) printPart.getMesh().getWorldBound()).getExtent().getZ() * 2);
				}
			}
		}
		paper.setSize(13.0 * 72.0, 19.0 * 72.0);
		// final double ratio = paper.getWidth() / paper.getHeight();
		// if (maxWidth / maxHeight > ratio) {
		// pageWidth = maxWidth + PRINT_MARGIN * 2;
		// pageHeight = pageWidth / ratio;
		// } else {
		// pageHeight = maxHeight + PRINT_MARGIN * 2;
		// pageWidth = pageHeight * ratio;
		// }
		final double exactCentimeterOnPaperScale = 1.0 / 72.0 / 4.0 / 10.6 * 10.8;
		pageWidth = paper.getWidth() * exactCentimeterOnPaperScale;
		pageHeight = paper.getHeight() * exactCentimeterOnPaperScale;
	}

	private void arrangePrintPages(final ArrayList<ArrayList<Spatial>> pages) {
		final double ratio = (double) Camera.getCurrentCamera().getWidth() / Camera.getCurrentCamera().getHeight();
		cols = (int) Math.round(Math.sqrt((pages.size() + 1) * ratio));
		if (cols % 2 == 0)
			cols++;
		rows = (int) Math.ceil((pages.size() + 1.0) / cols);

		int pageNum = 0;
		printCenters.clear();
		for (final ArrayList<Spatial> page : pages) {
			final Vector3 currentCorner = new Vector3();
			double x, y;
			do {
				x = (pageNum % cols - cols / 2) * (pageWidth + SPACE_BETWEEN_PAGES);
				y = (pageNum / cols - (rows - 1) / 2) * (pageHeight + SPACE_BETWEEN_PAGES);
				currentCorner.setX(x - pageWidth / 2);
				currentCorner.setZ(y + pageHeight / 2);
				pageNum++;
			} while (currentCorner.length() < pageWidth);

			printCenters.add(new Vector3(x, 0, y));

			for (final Spatial printSpatial : page)
				((UserData) printSpatial.getUserData()).getPrintCenter().addLocal(currentCorner);

			final Box box = new Box("Page Boundary");
			box.setData(currentCorner.add(0, 0.1, 0, null), currentCorner.add(pageWidth, 0.2, -pageHeight, null));
			box.setDefaultColor(ColorRGBA.GRAY);
			pagesRoot.attachChild(box);
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
//							roof.flatten(1.0);
							computePrintCenterOf(mesh, pages);
						}
				} else {
					final Mesh mesh = printPart.getMesh();
					printPart.setPrintVertical(decideVertical(mesh));
//					printPart.flattenInit();
//					printPart.flatten(1.0);
					computePrintCenterOf(mesh, pages);
				}
			}
		}
	}

	private boolean decideVertical(final Mesh mesh) {
		final OrientedBoundingBox bound = (OrientedBoundingBox) mesh.getWorldBound();
		final boolean isMeshVertical = bound.getExtent().getX() < bound.getExtent().getZ();
		return isMeshVertical && bound.getExtent().getZ() * 2 < pageWidth - PAGE_MARGIN * 2 || !isMeshVertical && bound.getExtent().getX() * 2 > pageWidth - PAGE_MARGIN * 2;
	}

	private void computePrintCenterOf(final Spatial printPart, final ArrayList<ArrayList<Spatial>> pages) {
		/* rotate 90 degrees if vertical and can fit horizontally or not vertical and cannot fit horizontally */
		final OrientedBoundingBox bound = (OrientedBoundingBox) printPart.getWorldBound();
		final boolean isVertical = bound.getExtent().getX() < bound.getExtent().getZ();
//		if (isVertical && bound.getExtent().getZ() < pageWidth - PAGE_MARGIN * 2 || !isVertical && bound.getExtent().getX() > pageWidth - PAGE_MARGIN * 2) {
//			printPart.getParent().setRotation(new Matrix3().fromAngles(0, -Math.PI / 2.0, 0).multiplyLocal(printPart.getParent().getRotation()));
//			printPart.getParent().updateWorldBound(true);
//		}
		
		boolean isFitted = false;
		for (int pageNum = 0; pageNum < pages.size() && !isFitted; pageNum++)
			isFitted = fitInPage(printPart, pages.get(pageNum));
		if (!isFitted) {
			// final BoundingBox bounds = (BoundingBox) printPart.getWorldBound();
			printPart.updateWorldBound(true);
			final OrientedBoundingBox bounds = (OrientedBoundingBox) printPart.getWorldBound();
			// ((UserData) printPart.getUserData()).setPrintCenter(new Vector3(bounds.getXExtent() + PAGE_AND_PART_MARGIN, 0, -bounds.getZExtent() - PAGE_AND_PART_MARGIN));
			((UserData) printPart.getUserData()).setPrintCenter(new Vector3(bounds.getExtent().getX() + PAGE_MARGIN, 0, -bounds.getExtent().getZ() - PAGE_MARGIN));
			final ArrayList<Spatial> page = new ArrayList<Spatial>();
			page.add(printPart);
			pages.add(page);
		}
	}

	private boolean fitInPage(final Spatial printPart, final ArrayList<Spatial> page) {
		int a;
		if (((UserData)printPart.getUserData()).getHousePart() instanceof Roof)
			a = 5;
		for (Spatial neighborPart : page) {
			final Vector3 neighborPartCenter = ((UserData) neighborPart.getUserData()).getPrintCenter();
			// final BoundingBox neighborBound = (BoundingBox) neighborPart.getWorldBound().clone(null);
			final OrientedBoundingBox neighborBound = (OrientedBoundingBox) neighborPart.getWorldBound().clone(null);
			// final boolean isRoof = ((UserData) printPart.getUserData()).getHousePart() instanceof Roof;
			final OrientedBoundingBox printPartBound;
			// if (isRoof)
			// printPartBound = computeBoundingBox((Mesh)printPart);
			// else
			// printPartBound = (BoundingBox) printPart.getWorldBound().clone(null);
			printPartBound = (OrientedBoundingBox) printPart.getWorldBound().clone(null);
			// final double SPACE_BETWEEN_PARTS = 0.3; //0.5;
			// final double xExtend = neighborBound.getXExtent() + printPartBound.getXExtent() + PADDING;
			// final double zExtend = neighborBound.getZExtent() + printPartBound.getZExtent() + PADDING;
			final double xExtend = neighborBound.getExtent().getX() + printPartBound.getExtent().getX() + SPACE_BETWEEN_PARTS;
			final double zExtend = neighborBound.getExtent().getZ() + printPartBound.getExtent().getZ() + SPACE_BETWEEN_PARTS;

			for (double angleQuarter = 0; angleQuarter < 4; angleQuarter++) {
				final boolean isHorizontal = angleQuarter % 2 == 0;
				final Vector3 tryCenter = new Matrix3().fromAngles(0, angleQuarter * Math.PI / 2.0, 0).applyPost(new Vector3(isHorizontal ? xExtend : zExtend, 0, 0), null);
				tryCenter.addLocal(neighborPartCenter);
				if (!isHorizontal)
					// tryCenter.setX(PAGE_AND_PART_MARGIN + printPartBound.getXExtent());
					tryCenter.setX(PAGE_MARGIN + printPartBound.getExtent().getX());

				if (!isHorizontal)
					// tryCenter.setX(MathUtils.clamp(tryCenter.getX(), PAGE_AND_PART_MARGIN + printPartBound.getXExtent(), pageWidth - PAGE_AND_PART_MARGIN - printPartBound.getXExtent()));
					tryCenter.setX(MathUtils.clamp(tryCenter.getX(), PAGE_MARGIN + printPartBound.getExtent().getX(), pageWidth - PAGE_MARGIN - printPartBound.getExtent().getX()));
				else
					tryCenter.setZ(MathUtils.clamp(tryCenter.getZ(), -pageHeight + PAGE_MARGIN + printPartBound.getExtent().getZ(), -PAGE_MARGIN - printPartBound.getExtent().getZ()));

				tryCenter.setY(0);

				boolean collision = false;
				// if (tryCenter.getX() - printPartBound.getXExtent() < PAGE_AND_PART_MARGIN || tryCenter.getX() + printPartBound.getXExtent() > pageWidth - PAGE_AND_PART_MARGIN || tryCenter.getZ() + printPartBound.getZExtent() > PAGE_AND_PART_MARGIN || tryCenter.getZ() - printPartBound.getZExtent() < -pageHeight + PAGE_AND_PART_MARGIN)
				if (tryCenter.getX() - printPartBound.getExtent().getX() < PAGE_MARGIN - MathUtils.ZERO_TOLERANCE || tryCenter.getX() + printPartBound.getExtent().getX() > pageWidth - PAGE_MARGIN + MathUtils.ZERO_TOLERANCE || tryCenter.getZ() + printPartBound.getExtent().getZ() > PAGE_MARGIN + MathUtils.ZERO_TOLERANCE || tryCenter.getZ() - printPartBound.getExtent().getZ() < -pageHeight + PAGE_MARGIN - MathUtils.ZERO_TOLERANCE)
					collision = true;
				else
					for (final Spatial otherPart : page) {
						printPartBound.setCenter(tryCenter);
						final BoundingVolume otherPartBound = otherPart.getWorldBound().clone(null);
						otherPartBound.setCenter(((UserData) otherPart.getUserData()).getPrintCenter());
						if (otherPartBound.intersects(printPartBound)) {
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

	// private BoundingBox computeBoundingBox(final Mesh printPart) {
	// final FloatBuffer buf = printPart.getMeshData().getVertexBuffer();
	// buf.rewind();
	// final FloatBuffer newbuf = BufferUtils.createFloatBuffer(buf.limit());
	// while (buf.hasRemaining()) {
	// final Vector3 v = new Vector3(buf.get(), buf.get(), buf.get());
	// printPart.getWorldTransform().applyForward(v);
	// newbuf.put(v.getXf()).put(v.getYf()).put(v.getZf());
	// }
	// final BoundingVolume boundingBox = printPart.getWorldBound();
	// System.out.println(boundingBox);
	// boundingBox.computeFromPoints(newbuf);
	// System.out.println(boundingBox);
	// return (BoundingBox) boundingBox;
	// }

	public double getPageWidth() {
		return pageWidth;
	}

	public double getPageHeight() {
		return pageHeight;
	}

	// public int getCols() {
	// return cols;
	// }
	//
	// public int getRows() {
	// return rows;
	// }
	//
	// public static int getMargin() {
	// return MARGIN;
	// }

	public ReadOnlyVector3 getZoomAllCameraLocation() {
		final double pageHeight = getPageHeight() + SPACE_BETWEEN_PAGES;
		final double w = cols * (getPageWidth() + SPACE_BETWEEN_PAGES);
		final double h = rows * pageHeight;
		return new Vector3(0, -Math.max(w, h), rows % 2 != 0 ? 0 : pageHeight / 2);
	}
}
