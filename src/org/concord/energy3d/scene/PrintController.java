package org.concord.energy3d.scene;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;

import org.concord.energy3d.MainFrame;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.util.ObjectCloner;
import org.concord.energy3d.util.PrintExporter;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.Updater;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.screen.ScreenExporter;

public class PrintController implements Updater {
	private static PrintController instance = new PrintController();
	private static double PAGE_WIDTH, PAGE_HEIGHT;
	private boolean isPrintPreview = false;
	private boolean init = false;
	private boolean finish = true;
	private int finishPhase = 100;
	private long startTime;
	private Scene sceneClone = null;
	private ArrayList<HousePart> printParts = new ArrayList<HousePart>();
	private double angle;
	private final ArrayList<Vector3> printCenters = new ArrayList<Vector3>();
	private boolean shadingSelected;
	private boolean shadowSelected;
	private Node pageBoundaryNode = new Node();

	public static PrintController getInstance() {
		return instance;
	}

	private PrintController() {

	}

	public void init() {

	}

	public void update(ReadOnlyTimer timer) {
		if (isPrintPreview)
			rotate();

		if (isFinished())
			return;

		final long time = timer.getTime();
		final Spatial originalHouseRoot = Scene.getInstance().getOriginalHouseRoot();
		if (init) {
			if (Util.DEBUG)
				System.out.println("Initializing Print Preview Animation...");
			init = false;
			startTime = time;
			HousePart.setFlatten(true);
			final CanvasRenderer renderer = SceneManager.getInstance().getCanvas().getCanvasRenderer();
			if (!isPrintPreview) { // && !renderer.getBackgroundColor().equals(ColorRGBA.WHITE))
				Scene.getRoot().detachChild(pageBoundaryNode);
				pageBoundaryNode.detachAllChildren();
			} else {
				renderer.makeCurrentContext();
				// renderer.getRenderer().setBackgroundColor(ColorRGBA.WHITE);
				renderer.releaseCurrentContext();
				HousePart.flattenPos = 0;
				if (Util.DEBUG)
					System.out.print("Deep cloning...");
				sceneClone = (Scene) ObjectCloner.deepCopy(Scene.getInstance());
				if (Util.DEBUG)
					System.out.println("done");
				printParts.clear();
				// HousePart.clearDrawFlags();				
				for (int i = 0; i < sceneClone.getParts().size(); i++) {
					final HousePart newPart = sceneClone.getParts().get(i);
					if (Util.DEBUG)
						System.out.println("Attaching Print Part...");
					Scene.getRoot().attachChild(newPart.getRoot());
					newPart.draw();
					newPart.setOriginal(Scene.getInstance().getParts().get(i));
					if (newPart.isPrintable() && newPart.isDrawCompleted())
						printParts.add(newPart);
				}
				// Scene.getRoot().updateWorldBound(false);
				// for (HousePart part : printParts)
				// System.out.println(part.getRoot().getWorldBound());

				final ArrayList<ArrayList<Spatial>> pages = new ArrayList<ArrayList<Spatial>>();
				computePageDimension();
				computePrintCenters(pages);
				computePrintCentersForRoofAndFloor(pages);
				arrangePrintPages(pages);
				System.out.println("Total # of Print Pages = " + pages.size());

				applyPreviewScale();
				SceneManager.getInstance().updatePrintPreviewScene(true);

				originalHouseRoot.setScale(2);
				originalHouseRoot.updateWorldBound(true);
				originalHouseRoot.setTranslation(0, 0, -Util.findBoundLength(originalHouseRoot.getWorldBound()) / 3);
			}
			for (HousePart part : Scene.getInstance().getParts())
				part.getRoot().getSceneHints().setCullHint(CullHint.Always);
			if (Util.DEBUG)
				System.out.println("Finished initialization of Print Preview Animation.");
		}

		if (!finish) {
			final double t = (time - startTime) / 1.0 / timer.getResolution();
			if (Util.DEBUG)
				System.out.println("t = " + t);
			HousePart.setFlattenTime(isPrintPreview ? t : 1 - t);
			drawPrintParts();

			finish = t > 1;
			finishPhase = 0;

			// for (HousePart part : printParts)
			// System.out.println(part.getRoot().getWorldBound());

		}

		if (finish) {
			if (Util.DEBUG)
				System.out.println("Finishing Print Preview Animation...");
			if (isPrintPreview)
				Scene.getRoot().attachChild(pageBoundaryNode);
			if (!isPrintPreview)
				HousePart.setFlatten(false);
			if (!isPrintPreview && finishPhase == 10) {
				originalHouseRoot.setRotation(new Matrix3().fromAngles(0, 0, 0));
				angle = 0;
				for (HousePart housePart : sceneClone.getParts())
					Scene.getRoot().detachChild(housePart.getRoot());
				printParts.clear();
				originalHouseRoot.setScale(1);
				originalHouseRoot.setTranslation(0, 0, 0);
				originalHouseRoot.updateGeometricState(timer.getTimePerFrame(), true);

				final CanvasRenderer renderer = SceneManager.getInstance().getCanvas().getCanvasRenderer();
				renderer.makeCurrentContext();
				renderer.getRenderer().setBackgroundColor(ColorRGBA.BLACK);
				renderer.releaseCurrentContext();

				final MainFrame frame = MainFrame.getInstance();
				frame.getLightingMenu().setSelected(shadingSelected);
				frame.getShadowMenu().setSelected(shadowSelected);

				SceneManager.getInstance().updatePrintPreviewScene(false);
				if (Util.DEBUG)
					System.out.println("Finished Print Preview Animation.");
			}

			if (finishPhase == 10) {
				for (HousePart part : Scene.getInstance().getParts())
					part.getRoot().getSceneHints().setCullHint(CullHint.Inherit);
				if (Util.DEBUG)
					System.out.println("Final Finish of Print Preview Animation.");

			}

			finishPhase++;

			// if (finishPhase > 20) {
			// counter++;
			// if (Util.DEBUG)
			// System.out.println("PrintPreview Counter: " + counter);
			// isPrintPreview = !isPrintPreview;
			// init = true;
			// finish = false;
			// }
		}
	}

	private void applyPreviewScale() {
		HousePart.clearPrintSpace();
		for (HousePart part : printParts) {
			part.updatePrintSpace();
		}

		HousePart.PRINT_COLS = (int) Math.ceil(Math.sqrt(printParts.size()));
	}

	public void drawPrintParts() {
		if (sceneClone == null)
			return;
//		printCenters.clear();
		for (HousePart part : sceneClone.getParts())
			// TODO If draw not completed then it shouldn't even exist at this point!
			if (part.isDrawCompleted())
				part.draw();
//		printCenters.size();
	}

	public void print() {		
		Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Always);
		PrintExporter printExporter = new PrintExporter();
		Camera camera = Camera.getCurrentCamera();
//		camera.resize(800, 1100);
		SceneManager.getInstance().resizeCamera(800, 1100);
		for (Vector3 pos : printCenters) {
			camera.setLocation(pos.getX(), pos.getY() - PAGE_WIDTH * 2, pos.getZ());
			camera.lookAt(pos.add(0, 1, 0, null), Vector3.UNIT_Z);
			SceneManager.getInstance().getCameraNode().updateFromCamera();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			final CanvasRenderer canvasRenderer = SceneManager.getInstance().getCanvas().getCanvasRenderer();
			canvasRenderer.makeCurrentContext();
			ScreenExporter.exportCurrentScreen(canvasRenderer.getRenderer(), printExporter);
			canvasRenderer.releaseCurrentContext();
		}
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(printExporter);
		if (job.printDialog())
			try {
				job.print();
			} catch (PrinterException exc) {
				System.out.println(exc);
			}
		Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Inherit);
		SceneManager.getInstance().resetCamera();
	}

	public void setPrintPreview(final boolean printPreview) {
		if (printPreview == isPrintPreview)
			return;
		init = true;
		finish = false;
		isPrintPreview = printPreview;

		final MainFrame frame = MainFrame.getInstance();
		if (printPreview) {
			shadingSelected = frame.getLightingMenu().isSelected();
			shadowSelected = frame.getShadowMenu().isSelected();

			if (shadingSelected)
				frame.getLightingMenu().setSelected(false);
			if (shadowSelected)
				frame.getShadowMenu().setSelected(false);
		}

		// SceneManager.getInstance().setCompassVisible(!printPreview);
		// if (printPreview)
		// SceneManager.getInstance().updatePrintPreviewScene(true);
	}

	public boolean isPrintPreview() {
		return isPrintPreview;
	}

	public void rotate() {
		angle += 0.01;
		Scene.getInstance().getOriginalHouseRoot().setRotation(new Matrix3().fromAngles(0, 0, angle));
	}

	public ArrayList<HousePart> getPrintParts() {
		return printParts;
	}

	public void addPrintCenters(Vector3 p) {
		printCenters.add(p);
	}

	public boolean isFinished() {
		return finish && finishPhase > 20;
	}

	private void computePageDimension() {
		double maxSize = 0;
		for (final HousePart printPart : printParts) {
			printPart.getRoot().updateWorldBound(true);
			double d = 2 + Util.findBoundLength(printPart.getRoot().getWorldBound());

			if (!Double.isInfinite(d) && d > maxSize)
				maxSize = d;
		}
		PAGE_WIDTH = maxSize;
		PAGE_HEIGHT = PAGE_WIDTH * 4 / 3;
	}

	private void arrangePrintPages(final ArrayList<ArrayList<Spatial>> pages) {
		int cols = (int) Math.round(Math.sqrt(pages.size() * 2));
		if (cols % 2 == 0)
			cols++;
		final int rows = (int) Math.ceil(cols / 2.0);

		int pageNum = 0;
		printCenters.clear();
		for (final ArrayList<Spatial> page : pages) {
			final Vector3 currentCorner = new Vector3();
			double x, y;
			do {
				final int margin = 5;
				x = (pageNum % cols - cols / 2) * (PAGE_WIDTH + margin);
				y = (-pageNum / cols + rows / 2) * (PAGE_HEIGHT + margin);
				currentCorner.setX(x - PAGE_WIDTH / 2);
				currentCorner.setZ(y + PAGE_HEIGHT / 2);
				pageNum++;
			} while (currentCorner.length() < PAGE_WIDTH);
			
			printCenters.add(new Vector3(x, 0, y));
			
			for (final Spatial printSpatial : page)
				((Vector3)printSpatial.getUserData()).addLocal(currentCorner);
			
			final Box box = new Box("Page Boundary");
			box.setData(currentCorner.add(0, 0.1, 0, null), currentCorner.add(PAGE_WIDTH, 0.2, -PAGE_HEIGHT, null));
			pageBoundaryNode.attachChild(box);
		}
		
	}

	private void computePrintCenters(final ArrayList<ArrayList<Spatial>> pages) {
		for (HousePart printPartOrg : printParts) {
			if (printPartOrg instanceof Roof)
				continue;
			Spatial printPart = printPartOrg.getRoot();
			computePrintCenterOf(printPart, pages);
		}
	}

	private void computePrintCentersForRoofAndFloor(final ArrayList<ArrayList<Spatial>> pages) {
		for (HousePart printPart : printParts) {
			if (!(printPart instanceof Roof))
				continue;
			for (Spatial roofPart : ((Roof) printPart).getFlattenedMeshesRoot().getChildren()) {
				computePrintCenterOf(roofPart, pages);
			}
		}
	}

	public void computePrintCenterOf(final Spatial printPart, final ArrayList<ArrayList<Spatial>> pages) {
		printPart.updateWorldBound(true);
		boolean isFitted = false;
		for (int pageNum = 0; pageNum < pages.size() && !isFitted; pageNum++) {
			isFitted = fitInPage(printPart, pages.get(pageNum));
		}
		if (!isFitted) {
			final double radius = Util.findBoundLength(printPart.getWorldBound()) / 2;
			printPart.setUserData(new Vector3(radius, 0, -radius));
			final ArrayList<Spatial> page = new ArrayList<Spatial>();
			page.add(printPart);
			pages.add(page);
		}
	}

	private boolean fitInPage(final Spatial printPart, final ArrayList<Spatial> page) {
		final double printPartRadius = Util.findBoundLength(printPart.getWorldBound()) / 2;
		for (Spatial part : page) {
			final Vector3 p = (Vector3) part.getUserData();
			final double r = Util.findBoundLength(part.getWorldBound()) / 2;
			final double dis = r + printPartRadius;

			final Vector3 disVector = new Vector3(dis, 0, 0);
			for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
				final Vector3 tryCenter = new Matrix3().fromAngles(0, angle, 0).applyPost(disVector, null);
				tryCenter.addLocal(p);
				boolean collision = false;
				if (!isCircleInsideRectangle(tryCenter, printPartRadius, new Vector3(), new Vector3(PAGE_WIDTH, 0, -PAGE_HEIGHT)))
					collision = true;
				else
					for (Spatial otherPart : page) {
						if (otherPart == part)
							continue;
						collision = tryCenter.subtract((Vector3) otherPart.getUserData(), null).length() < printPartRadius + Util.findBoundLength(otherPart.getWorldBound()) / 2;
						if (collision)
							break;
					}
				if (!collision) {
					printPart.setUserData(tryCenter);
					page.add(printPart);
					return true;
				}
			}
		}
		return false;
	}

	private boolean isCircleInsideRectangle(Vector3 center, double r, Vector3 p1, Vector3 p2) {
		for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 2) {
			final Vector3 p = new Matrix3().fromAngles(0, angle, 0).applyPost(new Vector3(r, 0, 0), null);
			p.addLocal(center);
			final double x = p.getX();
			final double z = p.getZ();
			if (x < p1.getX() || x > p2.getX() || z > p1.getZ() || z < p2.getZ())
				return false;
		}
		return true;
	}

}