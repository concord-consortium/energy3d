package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager.Operation;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class ChangeBuildingColorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private ReadOnlyColorRGBA orgColor, newColor;
	private Foundation foundation;
	private Operation operation;

	public ChangeBuildingColorCommand(Foundation foundation, Operation operation) {
		this.foundation = foundation;
		this.operation = operation;
		orgColor = Scene.getInstance().getPartColorForWholeBuilding(foundation, operation);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newColor = Scene.getInstance().getPartColorForWholeBuilding(foundation, operation);
		Scene.getInstance().setPartColorForWholeBuilding(foundation, operation, orgColor);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setPartColorForWholeBuilding(foundation, operation, newColor);
		Scene.getInstance().redrawAll();
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
		return "Color Change for Whole Building";
	}

}
