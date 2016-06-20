package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Window;

public class ChangeWindowShuttersCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldLeftShutter, newLeftShutter;
	private boolean oldRightShutter, newRightShutter;
	private Window window;

	public ChangeWindowShuttersCommand(Window selectedWindow) {
		this.window = selectedWindow;
		oldLeftShutter = selectedWindow.getLeftShutter();
		oldRightShutter = selectedWindow.getRightShutter();
	}

	public Window getWindow() {
		return window;
	}

	public boolean getOldLeftShutter() {
		return oldLeftShutter;
	}

	public boolean getOldRightShutter() {
		return oldRightShutter;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newLeftShutter = window.getLeftShutter();
		window.setLeftShutter(oldLeftShutter);
		newRightShutter = window.getRightShutter();
		window.setRightShutter(oldRightShutter);
		window.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		window.setLeftShutter(newLeftShutter);
		window.setRightShutter(newRightShutter);
		window.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Shutters for Window";
	}

}
