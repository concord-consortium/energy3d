package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeGroundThermalDiffusivityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;

	public ChangeGroundThermalDiffusivityCommand() {
		oldValue = Scene.getInstance().getGround().getThermalDiffusivity();
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getGround().getThermalDiffusivity();
		Scene.getInstance().getGround().setThermalDiffusivity(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().getGround().setThermalDiffusivity(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Change Ground Thermal Diffusivity";
	}

}
