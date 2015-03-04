package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChangeSolarPanelEfficiencyCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgEfficiency, newEfficiency;
	private SolarPanel selectedSolarPanel;

	public ChangeSolarPanelEfficiencyCommand(SolarPanel selectedSolarPanel) {
		this.selectedSolarPanel = selectedSolarPanel;
		orgEfficiency = selectedSolarPanel.getEfficiency();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newEfficiency = selectedSolarPanel.getEfficiency();
		selectedSolarPanel.setEfficiency(orgEfficiency);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		selectedSolarPanel.setEfficiency(newEfficiency);
	}

	// for action logging
	public SolarPanel getSolarPanel() {
		return selectedSolarPanel;
	}

	@Override
	public String getPresentationName() {
		return "Efficiency Change for Selected Solar Panel";
	}

}
