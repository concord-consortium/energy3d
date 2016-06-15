package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class ChangeShutterLengthCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private Window window;

	public ChangeShutterLengthCommand(Window window) {
		this.window = window;
		oldValue = window.getShutterLength();
	}

	public Window getWindow() {
		return window;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = window.getShutterLength();
		window.setShutterLength(oldValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		window.setShutterLength(newValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Shutter Length Change for Selected Window";
	}

}
