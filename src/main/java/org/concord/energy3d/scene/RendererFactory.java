package org.concord.energy3d.scene;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.FocusWrapper;
import com.ardor3d.input.KeyboardWrapper;
import com.ardor3d.input.MouseWrapper;

public abstract class RendererFactory {
	protected Canvas canvas;
	protected MouseWrapper mouseWrapper;
	protected KeyboardWrapper keyboardWrapper;
	protected FocusWrapper focusWrapper;

	public Canvas getCanvas() {
		return canvas;
	}

	public MouseWrapper getMouseWrapper() {
		return mouseWrapper;
	}

	public KeyboardWrapper getKeyboardWrapper() {
		return keyboardWrapper;
	}

	public FocusWrapper getFocusWrapper() {
		return focusWrapper;
	}

}
