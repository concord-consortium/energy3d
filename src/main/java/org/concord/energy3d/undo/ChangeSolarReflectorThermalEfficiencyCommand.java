package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarReflector;

public class ChangeSolarReflectorThermalEfficiencyCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final SolarReflector reflector;

	public ChangeSolarReflectorThermalEfficiencyCommand(final SolarReflector reflector) {
		this.reflector = reflector;
		oldValue = reflector.getThermalEfficiency();
	}

	public SolarReflector getSolarReflector() {
		return reflector;
	}

	public double getOldValue() {
		return oldValue;
	}

	public double getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = reflector.getThermalEfficiency();
		reflector.setThermalEfficiency(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		reflector.setThermalEfficiency(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Thermal Efficiency Change for Selected " + reflector.getClass().getSimpleName();
	}

}
