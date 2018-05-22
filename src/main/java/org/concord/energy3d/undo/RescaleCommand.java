package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class RescaleCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;

	public RescaleCommand() {
		oldValue = Scene.getInstance().getScale();
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getScale();
		Scene.getInstance().setScale(oldValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setScale(newValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Rescale";
	}

}
