package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;

public class ChangeParabolicTroughThermalEfficiencyCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final ParabolicTrough trough;

	public ChangeParabolicTroughThermalEfficiencyCommand(final ParabolicTrough trough) {
		this.trough = trough;
		oldValue = trough.getThermalEfficiency();
	}

	public ParabolicTrough getParabolicTrough() {
		return trough;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = trough.getThermalEfficiency();
		trough.setThermalEfficiency(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		trough.setThermalEfficiency(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Thermal Efficiency Change for Selected Parabolic Trough";
	}

}
