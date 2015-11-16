package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeGroundThermalDiffusivityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgThermalDiffusivity, newThermalDiffusivity;

	public ChangeGroundThermalDiffusivityCommand() {
		orgThermalDiffusivity = Scene.getInstance().getGround().getThermalDiffusivity();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newThermalDiffusivity = Scene.getInstance().getGround().getThermalDiffusivity();
		Scene.getInstance().getGround().setThermalDiffusivity(orgThermalDiffusivity);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().getGround().setThermalDiffusivity(newThermalDiffusivity);
	}

	@Override
	public String getPresentationName() {
		return "Change Ground Thermal Diffusivity";
	}

}
