package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.SceneManager;

public class ShowShadowCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;

	public ShowShadowCommand() {
		oldValue = SceneManager.getInstance().isShadowEnabled();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = SceneManager.getInstance().isShadowEnabled();
		SceneManager.getInstance().setShadow(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().setShadow(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Show Shadow";
	}

}
