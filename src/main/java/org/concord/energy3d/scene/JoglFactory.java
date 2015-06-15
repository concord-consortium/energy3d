package org.concord.energy3d.scene;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.JoglSwingCanvas;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;

public class JoglFactory extends RendererFactory {

	public JoglFactory(final DisplaySettings settings, final SceneManager sceneManager) {
		final JoglSwingCanvas canvas = new JoglSwingCanvas(settings, new JoglCanvasRenderer(sceneManager));
		mouseWrapper = new AwtMouseWrapper(canvas, new AwtMouseManager(canvas));
		keyboardWrapper = new AwtKeyboardWrapper(canvas);
		focusWrapper = new AwtFocusWrapper(canvas);
		this.canvas = canvas;
	}

}
