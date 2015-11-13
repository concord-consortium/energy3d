package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager.Operation;

public class ChangeBuildingUValueCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgUFactor, newUFactor;
	private Foundation foundation;
	private Operation operation;

	public ChangeBuildingUValueCommand(Foundation foundation, Operation operation) {
		this.foundation = foundation;
		this.operation = operation;
		orgUFactor = Scene.getInstance().getPartUFactorForWholeBuilding(foundation, operation);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newUFactor = Scene.getInstance().getPartUFactorForWholeBuilding(foundation, operation);
		Scene.getInstance().setPartUFactorForWholeBuilding(foundation, operation, orgUFactor);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setPartUFactorForWholeBuilding(foundation, operation, newUFactor);
	}

	// for action logging
	public Foundation getFoundation() {
		return foundation;
	}

	// for action logging
	public Operation getOperation() {
		return operation;
	}

	@Override
	public String getPresentationName() {
		return "U-Factor Change for Whole Building";
	}

}
