package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeBackgroundAlbedoCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;

	public ChangeBackgroundAlbedoCommand() {
		oldValue = Scene.getInstance().getGround().getAlbedo();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getGround().getAlbedo();
		Scene.getInstance().getGround().setAlbedo(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().getGround().setAlbedo(newValue);
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public String getPresentationName() {
		return "Change Ground Albedo";
	}

}
