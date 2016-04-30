package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChangeSolarPanelEfficiencyCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private SolarPanel solarPanel;

	public ChangeSolarPanelEfficiencyCommand(SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.getEfficiency();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = solarPanel.getEfficiency();
		solarPanel.setEfficiency(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setEfficiency(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Efficiency Change for Selected Solar Panel";
	}

}
