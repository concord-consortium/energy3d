package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;

public class ChangeBuildingSolarPanelEfficiencyCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgEfficiency, newEfficiency;
	private Foundation foundation;

	public ChangeBuildingSolarPanelEfficiencyCommand(Foundation foundation) {
		this.foundation = foundation;
		orgEfficiency = Scene.getInstance().getSolarPanelEfficiencyForWholeBuilding(foundation);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newEfficiency = Scene.getInstance().getSolarPanelEfficiencyForWholeBuilding(foundation);
		Scene.getInstance().setSolarPanelEfficiencyForWholeBuilding(foundation, orgEfficiency);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setSolarPanelEfficiencyForWholeBuilding(foundation, newEfficiency);
	}

	// for action logging
	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public String getPresentationName() {
		return "Efficiency Change for All Solar Panels of Selected Building";
	}

}
