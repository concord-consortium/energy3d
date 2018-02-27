package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeHeliostatTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;

	public ChangeHeliostatTextureCommand() {
		oldValue = Scene.getInstance().getHeliostatTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getHeliostatTextureType();
		Scene.getInstance().setHeliostatTextureType(oldValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setHeliostatTextureType(newValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Heliostat Texture Change";
	}

}
