package org.concord.energy3d.scene;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.awt.JoglNewtAwtCanvas;
import com.ardor3d.input.jogl.JoglNewtFocusWrapper;
import com.ardor3d.input.jogl.JoglNewtKeyboardWrapper;
import com.ardor3d.input.jogl.JoglNewtMouseManager;
import com.ardor3d.input.jogl.JoglNewtMouseWrapper;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;

public class JoglNewtFactory extends RendererFactory {

	public JoglNewtFactory(final DisplaySettings settings, final SceneManager sceneManager) {
		final JoglNewtAwtCanvas canvas = new JoglNewtAwtCanvas(settings, new JoglCanvasRenderer(sceneManager));
		TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
		mouseWrapper = new JoglNewtMouseWrapper(canvas, new JoglNewtMouseManager(canvas));
		keyboardWrapper = new JoglNewtKeyboardWrapper(canvas);
		focusWrapper = new JoglNewtFocusWrapper(canvas);
		this.canvas = canvas;
	}

}
