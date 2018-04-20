package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Foundation foundation;

	public ChangeFoundationTextureCommand(final Foundation foundation) {
		this.foundation = foundation;
		oldValue = foundation.getTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = foundation.getTextureType();
		foundation.setTextureType(oldValue);
		foundation.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.setTextureType(newValue);
		foundation.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Foundation Texture Change";
	}

}
