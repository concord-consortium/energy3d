package org.concord.energy3d.scene;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.MainFrame;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.util.ObjectCloner;
import org.concord.energy3d.util.PrintExporter;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.Updater;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.screen.ScreenExporter;

public class PrintController implements Updater {
	private static PrintController instance = new PrintController();
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
			if (isPrintPreview) { // && !renderer.getBackgroundColor().equals(ColorRGBA.WHITE))
				renderer.makeCurrentContext();
				renderer.getRenderer().setBackgroundColor(ColorRGBA.WHITE);
				renderer.releaseCurrentContext();
				HousePart.flattenPos = 0;
				if (Util.DEBUG)
					System.out.print("Deep cloning...");
				sceneClone = (Scene) ObjectCloner.deepCopy(Scene.getInstance());
				if (Util.DEBUG)
					System.out.println("done");
				printParts.clear();
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
				
				computePrintCenters();
				
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
		}

		if (finish) {
			if (Util.DEBUG)
				System.out.println("Finishing Print Preview Animation...");
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
		printCenters.clear();
		for (HousePart part : sceneClone.getParts())
			// TODO If draw not completed then it shouldn't even exist at this point!
			if (part.isDrawCompleted())
				part.draw();
		printCenters.size();
	}

	public void print() {
		Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Always);
		PrintExporter printExporter = new PrintExporter();
		Camera camera = Camera.getCurrentCamera();
		for (Vector3 pos : printCenters) {
			camera.setLocation(pos.getX(), pos.getY() - HousePart.PRINT_SPACE, pos.getZ());
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
	
	private void computePrintCenters() {
		final ArrayList<ArrayList<HousePart>> pages = new ArrayList<ArrayList<HousePart>>();
		for (HousePart printPart : printParts) {
			boolean isFitted = false;
			for (int pageNum = 0; pageNum < pages.size() && !isFitted; pageNum++) {
				final ArrayList<HousePart> page = pages.get(pageNum);
				isFitted = fitInPage(printPart, page, pageNum); 
			}
			if (!isFitted) {
				final ArrayList<HousePart> page = new ArrayList<HousePart>();
				page.add(printPart);
				pages.add(page);
			}
		}
	}

	private boolean fitInPage(final HousePart printPart, final ArrayList<HousePart> page, final int pageNum) {
		final Vector3 pageCorner = getPageCorner(pageNum);
		final Vector3 v1 = pageCorner.add(3, 0, 0, null);
		final Vector3 v2 = pageCorner.add(0, -3, 0, null);
		final double printPartRadius = ((BoundingSphere)printPart.getRoot().getWorldBound()).getRadius();		
		for (HousePart part : page) {
			final Vector3 p = part.getPrintCenter();
			final double r = ((BoundingSphere)part.getRoot().getWorldBound()).getRadius();
			final double dis = r + printPartRadius;
			
			final Vector3 disVector = new Vector3(dis, 0, 0);
			for (double angle = 0; angle < Math.PI*2 ; angle += Math.PI / 4) {
				final Vector3 tryCenter = new Matrix3().fromAngles(0, 0, angle).applyPost(disVector, null);
				tryCenter.addLocal(p);
				
				boolean collision = false;
				for (HousePart otherPart : page) {
					if (otherPart == part)
						continue;
					collision = tryCenter.subtract(otherPart.getPrintCenter(), null).length() < printPartRadius + ((BoundingSphere)otherPart.getRoot().getWorldBound()).getRadius();
					final Vector3 v = tryCenter.subtract(pageCorner, null);
					collision = collision || (0<=v.dot(v1) && v.dot(v1)<=v1.dot(v1) && 0<=v.dot(v2) && v.dot(v2)<=v2.dot(v2));
					if (collision)
						break;
				}
				if (!collision) {
					printPart.setPrintCenter(tryCenter);
					page.add(printPart);
					return true;
				}
			}
		}
		return false;
	}

	private Vector3 getPageCorner(int pageNum) {		
		return new Vector3(pageNum * 3, 0, 0);
	}
}