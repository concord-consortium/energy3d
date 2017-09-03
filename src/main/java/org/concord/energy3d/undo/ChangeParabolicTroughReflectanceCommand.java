package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;

public class ChangeParabolicTroughReflectanceCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final ParabolicTrough trough;

	public ChangeParabolicTroughReflectanceCommand(final ParabolicTrough trough) {
		this.trough = trough;
		oldValue = trough.getReflectance();
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
		newValue = trough.getReflectance();
		trough.setReflectance(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		trough.setReflectance(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Reflectance Change for Selected Parabolic Trough";
	}

}
