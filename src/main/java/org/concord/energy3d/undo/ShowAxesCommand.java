package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.SceneManager;

public class ShowAxesCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;

	public ShowAxesCommand() {
		oldValue = SceneManager.getInstance().areAxesShown();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = SceneManager.getInstance().areAxesShown();
		SceneManager.getInstance().showAxes(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().showAxes(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Show Axes";
	}

}
