package org.concord.energy3d.scene;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.FocusWrapper;
import com.ardor3d.input.KeyboardWrapper;
import com.ardor3d.input.MouseWrapper;

public abstract class RendererFactory {

    Canvas canvas;
    MouseWrapper mouseWrapper;
    KeyboardWrapper keyboardWrapper;
    FocusWrapper focusWrapper;

    public Canvas getCanvas() {
        return canvas;
    }

    MouseWrapper getMouseWrapper() {
        return mouseWrapper;
    }

    KeyboardWrapper getKeyboardWrapper() {
        return keyboardWrapper;
    }

    FocusWrapper getFocusWrapper() {
        return focusWrapper;
    }

}