package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;

public class ChangeBuildingWindowShgcCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgShgc, newShgc;
	private Foundation foundation;

	public ChangeBuildingWindowShgcCommand(Foundation foundation) {
		this.foundation = foundation;
		orgShgc = Scene.getInstance().getWindowShgcForWholeBuilding(foundation);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newShgc = Scene.getInstance().getWindowShgcForWholeBuilding(foundation);
		Scene.getInstance().setWindowShgcForWholeBuilding(foundation, orgShgc);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setWindowShgcForWholeBuilding(foundation, newShgc);
	}

	// for action logging
	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public String getPresentationName() {
		return "SHGC Change for All Windows of Selected Building";
	}

}
