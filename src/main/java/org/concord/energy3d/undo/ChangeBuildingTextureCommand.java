package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;

public class ChangeBuildingTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final TextureMode oldValue;
	private TextureMode newValue;

	public ChangeBuildingTextureCommand() {
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
		return "Building Texture Change";
	}

}
