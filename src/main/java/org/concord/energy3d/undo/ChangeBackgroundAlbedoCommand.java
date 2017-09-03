package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeBackgroundAlbedoCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;

	public ChangeBackgroundAlbedoCommand() {
		oldValue = Scene.getInstance().getGround().getAlbedo();
	}

	public double getOldValue() {
		return oldValue;
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

	@Override
	public String getPresentationName() {
		return "Change Ground Albedo";
	}

}
