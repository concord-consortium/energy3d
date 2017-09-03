package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.SceneManager;

public class ZoomCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final boolean value;

	public ZoomCommand(final boolean value) {
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		SceneManager.getInstance().zoom(!value);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().zoom(value);
	}

	@Override
	public String getPresentationName() {
		return "Zoom";
	}

}
