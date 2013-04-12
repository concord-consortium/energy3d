package org.concord.energy3d.scene;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.lwjgl.LwjglAwtCanvas;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.lwjgl.LwjglTextureRendererProvider;

public class LwjglFactory extends RendererFactory {

	public LwjglFactory(final DisplaySettings settings, final SceneManager sceneManager) {
		try {
			final LwjglAwtCanvas canvas = new LwjglAwtCanvas(settings, new LwjglCanvasRenderer(sceneManager));
			TextureRendererFactory.INSTANCE.setProvider(new LwjglTextureRendererProvider());
			mouseWrapper = new AwtMouseWrapper(canvas, new AwtMouseManager(canvas));
			keyboardWrapper = new AwtKeyboardWrapper(canvas);
			focusWrapper = new AwtFocusWrapper(canvas);
			this.canvas = canvas;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
