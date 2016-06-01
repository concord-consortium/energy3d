package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChangeMicroInverterEfficiencyCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private SolarPanel solarPanel;

	public ChangeMicroInverterEfficiencyCommand(SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.getInverterEfficiency();
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
		newValue = solarPanel.getInverterEfficiency();
		solarPanel.setInverterEfficiency(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setInverterEfficiency(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Micro Inverter Efficiency Change for Selected Solar Panel";
	}

}
