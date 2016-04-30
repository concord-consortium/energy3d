package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;

public class ChangeTextureCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private TextureMode oldValue, newValue;

	public ChangeTextureCommand() {
		oldValue = Scene.getInstance().getTextureMode();
	}

	public TextureMode getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getTextureMode();
		Scene.getInstance().setTextureMode(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setTextureMode(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Texture Change";
	}

}
