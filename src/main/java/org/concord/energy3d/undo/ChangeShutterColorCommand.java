package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Window;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeShutterColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA oldColor, newColor;
	private Window window;

	public ChangeShutterColorCommand(Window window) {
		this.window = window;
		oldColor = window.getShutterColor();
	}

	public Window getWindow() {
		return window;
	}

	public ReadOnlyColorRGBA getOldColor() {
		return oldColor;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newColor = window.getShutterColor();
		window.setShutterColor(oldColor);
		window.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		window.setShutterColor(newColor);
		window.draw();
	}

	@Override
	public String getPresentationName() {
		return "Shutter Color Change for Selected Window";
	}

}
