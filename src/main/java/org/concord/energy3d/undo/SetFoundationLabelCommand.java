package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;

public class SetFoundationLabelCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelPowerTowerOutput;
	private final boolean oldLabelPowerTowerHeight;
	private final boolean oldLabelSolarPotential;
	private final boolean oldLabelBuildingEnergy;
	private boolean newLabelId;
	private boolean newLabelPowerTowerOutput;
	private boolean newLabelPowerTowerHeight;
	private boolean newLabelSolarPotential;
	private boolean newLabelBuildingEnergy;
	private final Foundation foundation;

	public SetFoundationLabelCommand(final Foundation foundation) {
		this.foundation = foundation;
		oldLabelId = foundation.getLabelId();
		oldLabelPowerTowerOutput = foundation.getLabelPowerTowerOutput();
		oldLabelPowerTowerHeight = foundation.getLabelPowerTowerHeight();
		oldLabelSolarPotential = foundation.getLabelSolarPotential();
		oldLabelBuildingEnergy = foundation.getLabelBuildingEnergy();
	}

	public Foundation getFoundation() {
		return foundation;
	}

	public boolean getOldLabelId() {
		return oldLabelId;
	}

	public boolean getNewLabelId() {
		return newLabelId;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newLabelId = foundation.getLabelId();
		newLabelPowerTowerOutput = foundation.getLabelPowerTowerOutput();
		newLabelPowerTowerHeight = foundation.getLabelPowerTowerHeight();
		newLabelSolarPotential = foundation.getLabelSolarPotential();
		newLabelBuildingEnergy = foundation.getLabelBuildingEnergy();
		foundation.setLabelId(oldLabelId);
		foundation.setLabelPowerTowerOutput(oldLabelPowerTowerOutput);
		foundation.setLabelPowerTowerHeight(oldLabelPowerTowerHeight);
		foundation.setLabelSolarPotential(oldLabelSolarPotential);
		foundation.setLabelBuildingEnergy(oldLabelBuildingEnergy);
		foundation.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		foundation.setLabelId(newLabelId);
		foundation.setLabelPowerTowerOutput(newLabelPowerTowerOutput);
		foundation.setLabelPowerTowerHeight(newLabelPowerTowerHeight);
		foundation.setLabelSolarPotential(newLabelSolarPotential);
		foundation.setLabelBuildingEnergy(newLabelBuildingEnergy);
		foundation.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Foundation";
	}

}
