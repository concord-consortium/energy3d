package org.concord.energy3d.scene;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.util.ObjectCloner;
import org.concord.energy3d.util.PrintExporter;
import org.concord.energy3d.util.Util;

import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
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
	private int counter = 0;
	private final ArrayList<Vector3> printCenters = new ArrayList<Vector3>();

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

		if (finish && finishPhase > 20)
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
//				renderer.makeCurrentContext();
//				renderer.getRenderer().setBackgroundColor(ColorRGBA.WHITE);
//				renderer.releaseCurrentContext();
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
//			final double t = 1;
			System.out.println("t = " + t);
			HousePart.setFlattenTime(isPrintPreview ? t : 1 - t);
//			clearPrintCenters();
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
				// HousePart.setFlatten(false);
				// for (HousePart part : parts)
				// part.draw();
				
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				
				originalHouseRoot.setRotation(new Matrix3().fromAngles(0, 0, 0));
				angle = 0;
				for (HousePart housePart : sceneClone.getParts())
					Scene.getRoot().detachChild(housePart.getRoot());
				printParts.clear();
				originalHouseRoot.setScale(1);
				originalHouseRoot.setTranslation(0, 0, 0);
				originalHouseRoot.updateGeometricState(timer.getTimePerFrame(), true);

//				final CanvasRenderer renderer = SceneManager.getInstance().getCanvas().getCanvasRenderer();
//				renderer.makeCurrentContext();
//				renderer.getRenderer().setBackgroundColor(ColorRGBA.BLACK);
//				renderer.releaseCurrentContext();				
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
			
//			if (finishPhase > 20) {
//				counter++;
//				if (Util.DEBUG)
//				System.out.println("PrintPreview Counter: " + counter);
//				isPrintPreview = !isPrintPreview;
//				init = true;
//				finish = false;
//			}
		}
	}

	private void applyPreviewScale() {
//		double maxW = 0;
//		double maxH = 0;
//		
		HousePart.clearPrintSpace();
		for (HousePart part : printParts) {
//			final double w = part.getPrintWidth();
//			final double h = part.getPrintHeight();
//			if (w > maxW)
//				maxW = w;
//			if (h > maxH)
//				maxH = h;
			part.updatePrintSpace();
		}
		
		HousePart.PRINT_COLS = (int)Math.ceil(Math.sqrt(printParts.size()));
//		
//		final double scale = HousePart.PRINT_SPACE / Math.max(maxW, maxH);
//		for (HousePart part : printParts)
//			part.getRoot().setScale(scale);
		
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
//		PrintExporter printExporter = new PrintExporter(PrintPreviewController.getInstance().getPrintParts().size());		
		PrintExporter printExporter = new PrintExporter();
//		double scale = 0.2;
//		root.setScale(scale);
		Camera camera = Camera.getCurrentCamera();
		// Vector3 location = new Vector3(camera.getLocation());
		// Vector3 direction = new Vector3(camera.getDirection());
		// ReadOnlyVector3 up = camera.getUp();
//		for (HousePart part : Scene.getInstance().getPrintParts()) {
//		for (HousePart part : PrintPreviewController.getInstance().getPrintParts()) {
		for (Vector3 pos: printCenters) {
			// if (printExporter.getCurrentPage() < Scene.getInstance().getPrintParts().size()) {
			// HousePart part = Scene.getInstance().getPrintParts().get(printExporter.getCurrentPage());
			// Vector3 pos = new Vector3(part.getPrintSequence() * scale, -5, part.getPrintY() * scale);
//			 Vector3 pos = new Vector3(part.getPrintSequence() % HousePart.PRINT_COLS * HousePart.PRINT_SPACE * scale, -5, part.getPrintSequence() / HousePart.PRINT_COLS * HousePart.PRINT_SPACE * scale);
//			Vector3 pos = part.getPrintCenter();
			System.out.println(pos);
//			camera.setLocation(pos.getX(), pos.getY() - 5, pos.getZ());
//			camera.setLocation(pos.getX(), pos.getY() - 7, pos.getZ());
//			camera.setLocation(pos.getX(), pos.getY() - HousePart.PRINT_SPACE * Math.sin(Math.PI / 2 - camera.getFovY()) / Math.sin(camera.getFovY()), pos.getZ());
			camera.setLocation(pos.getX(), pos.getY() - HousePart.PRINT_SPACE, pos.getZ());
			camera.lookAt(pos.add(0, 1, 0, null), Vector3.UNIT_Z);
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
		// camera.setLocation(location);
		// camera.lookAt(location.addLocal(direction), up);
//			resetCamera(viewMode);
			Scene.getInstance().getOriginalHouseRoot().getSceneHints().setCullHint(CullHint.Inherit);
			SceneManager.getInstance().resetCamera();
	}	

	public void setPrintPreview(final boolean printPreview) {
		if (printPreview == isPrintPreview)
			return;
		init = true;
		finish = false;
		isPrintPreview = printPreview;
		// if (printPreview)
		// SceneManager.getInstance().updatePrintPreviewScene(true);
	}

	public void rotate() {
		angle += 0.01;
		Scene.getInstance().getOriginalHouseRoot().setRotation(new Matrix3().fromAngles(0, 0, angle));
	}

	public ArrayList<HousePart> getPrintParts() {
		return printParts;
	}

//	public void clearPrintCenters() {
//		printCenters.clear();
//	}
	
	public void addPrintCenters(Vector3 p) {
		printCenters.add(p);
	}	
}
