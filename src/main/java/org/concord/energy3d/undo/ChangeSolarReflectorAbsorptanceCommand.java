package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarReflector;

public class ChangeSolarReflectorAbsorptanceCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final SolarReflector reflector;

	public ChangeSolarReflectorAbsorptanceCommand(final SolarReflector reflector) {
		this.reflector = reflector;
		oldValue = reflector.getAbsorptance();
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
		newValue = reflector.getAbsorptance();
		reflector.setAbsorptance(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		reflector.setAbsorptance(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Absorptance Change for Selected " + reflector.getClass().getSimpleName();
	}

}
