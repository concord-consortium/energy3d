package org.concord.energy3d.scene;

import java.util.ArrayList;

import org.concord.energy3d.model.HousePart;

import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;

public class PrintPreviewController implements Updater {
	private static PrintPreviewController instance = new PrintPreviewController();
	private boolean isPrintPreview = false;
	private boolean init = false;
	private boolean finish = true;
	private int finishPhase = 100;
	private long startTime;
	private Scene sceneClone = null;
	private ArrayList<HousePart> printParts = new ArrayList<HousePart>();
	private double angle;

	public static PrintPreviewController getInstance() {
		return instance;
	}

	private PrintPreviewController() {

	}

	public void init() {

	}

	public void update(ReadOnlyTimer timer) {
		if (isPrintPreview)
			rotate();

		if (finish && finishPhase > 1)
			return;

		final long time = timer.getTime();
		if (init) {
			init = false;
			startTime = time;
			final JoglCanvasRenderer renderer = (JoglCanvasRenderer) SceneManager.getInstance().getCanvas().getCanvasRenderer();
			if (isPrintPreview) { // && !renderer.getBackgroundColor().equals(ColorRGBA.WHITE))
				renderer.setCurrentContext();
				renderer.getRenderer().setBackgroundColor(ColorRGBA.WHITE);
				renderer.releaseCurrentContext();
				SceneManager.getInstance().updatePrintPreviewScene(true);
				HousePart.flattenPos = 0;
				sceneClone = (Scene) ObjectCloner.deepCopy(Scene.getInstance());
				printParts.clear();
				for (int i = 0; i < sceneClone.getParts().size(); i++) {
					final HousePart newPart = sceneClone.getParts().get(i);
					Scene.getRoot().attachChild(newPart.getRoot());
					newPart.draw();
					newPart.setOriginal(Scene.getInstance().getParts().get(i));
					if (newPart.isPrintable() && newPart.isDrawCompleted())
						printParts.add(newPart);
				}
				Scene.getInstance().getOriginalHouseRoot().setScale(2);
			}
			// for (HousePart part : Scene.getInstance().getParts())
			// part.getRoot().getSceneHints().setCullHint(CullHint.Always);
		}

		if (!finish) {
			final double t = Math.min(1, (time - startTime) / 1.0 / timer.getResolution());
			HousePart.setFlattenTime(isPrintPreview ? t : 1 - t);
			for (HousePart part : sceneClone.getParts())
				// TODO If draw not completed then it shouldn't even exist at this point!
				if (part.isDrawCompleted())
					part.draw();

			finish = t >= 1;
			finishPhase = 0;
		}

		if (finish) {
			if (!isPrintPreview && finishPhase == 0) {
				// HousePart.setFlatten(false);
				// for (HousePart part : parts)
				// part.draw();
				Scene.getInstance().getOriginalHouseRoot().setRotation(new Matrix3().fromAngles(0, 0, 0));
				angle = 0;
				for (HousePart housePart : sceneClone.getParts())
					Scene.getRoot().detachChild(housePart.getRoot());
				Scene.getInstance().getOriginalHouseRoot().setScale(1);
				final JoglCanvasRenderer renderer = (JoglCanvasRenderer) SceneManager.getInstance().getCanvas().getCanvasRenderer();
				renderer.setCurrentContext();
				renderer.getRenderer().setBackgroundColor(ColorRGBA.BLACK);
				renderer.releaseCurrentContext();				
			}
			
			if (!isPrintPreview && finishPhase == 1) {
				SceneManager.getInstance().updatePrintPreviewScene(false);				
			}

			// for (HousePart part : Scene.getInstance().getParts())
			// part.getRoot().getSceneHints().setCullHint(CullHint.Inherit);
		}
		finishPhase++;
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
}
