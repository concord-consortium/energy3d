package org.concord.energy3d.scene;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.JoglSwingCanvas;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.jogamp.nativewindow.ScalableSurface;

public class JoglFactory extends RendererFactory {

	public JoglFactory(final DisplaySettings settings, final SceneManager sceneManager) {
		final JoglSwingCanvas canvas = new JoglSwingCanvas(settings, new JoglCanvasRenderer(sceneManager));
		canvas.setSurfaceScale(new float[] {ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE});
		TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
		mouseWrapper = new AwtMouseWrapper(canvas, new AwtMouseManager(canvas));
		keyboardWrapper = new AwtKeyboardWrapper(canvas);
		focusWrapper = new AwtFocusWrapper(canvas);
		this.canvas = canvas;
	}

}
