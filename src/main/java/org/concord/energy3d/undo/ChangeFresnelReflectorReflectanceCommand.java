package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.FresnelReflector;

public class ChangeFresnelReflectorReflectanceCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final FresnelReflector reflector;

	public ChangeFresnelReflectorReflectanceCommand(final FresnelReflector reflector) {
		this.reflector = reflector;
		oldValue = reflector.getReflectance();
	}

	public FresnelReflector getFresnelReflector() {
		return reflector;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = reflector.getReflectance();
		reflector.setReflectance(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		reflector.setReflectance(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Reflectance Change for Selected Fresnel Reflector";
	}

}
