package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarCollector;

public class ChangePoleHeightCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final SolarCollector part;

	public ChangePoleHeightCommand(final SolarCollector part) {
		this.part = part;
		oldValue = part.getPoleHeight();
	}

	public SolarCollector getPart() {
		return part;
	}

	public double getOldValue() {
		return oldValue;
	}

	public double getNewValue() {
		newValue = part.getPoleHeight();
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = part.getPoleHeight();
		part.setPoleHeight(oldValue);
		if (part instanceof HousePart) {
			((HousePart) part).draw();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setPoleHeight(newValue);
		if (part instanceof HousePart) {
			((HousePart) part).draw();
		}
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for " + part.getClass().getSimpleName();
	}

}
