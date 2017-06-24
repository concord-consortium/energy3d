package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;

public class ChangeParabolicTroughReflectivityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final ParabolicTrough trough;

	public ChangeParabolicTroughReflectivityCommand(final ParabolicTrough trough) {
		this.trough = trough;
		oldValue = trough.getReflectivity();
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
		newValue = trough.getReflectivity();
		trough.setReflectivity(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		trough.setReflectivity(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Reflectivity Change for Selected Parabolic Trough";
	}

}
