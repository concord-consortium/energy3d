package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChangeInverterEfficiencyCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final SolarPanel solarPanel;

	public ChangeInverterEfficiencyCommand(final SolarPanel solarPanel) {
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
		return "Inverter Efficiency Change for Selected Solar Panel";
	}

}
