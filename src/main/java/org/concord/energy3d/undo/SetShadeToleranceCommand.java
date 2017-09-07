package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class SetShadeToleranceCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final SolarPanel sp;

	public SetShadeToleranceCommand(final SolarPanel sp) {
		this.sp = sp;
		oldValue = sp.getShadeTolerance();
	}

	public SolarPanel getSolarPanel() {
		return sp;
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = sp.getShadeTolerance();
		sp.setShadeTolerance(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		sp.setShadeTolerance(newValue);
	}

	@Override
	public String getPresentationName() {
		switch (oldValue) {
		case SolarPanel.NO_SHADE_TOLERANCE:
			return "Choose No Shade Tolerance";
		case SolarPanel.HIGH_SHADE_TOLERANCE:
			return "Choose High Shade Tolerance";
		default:
			return "Choose Partial Shade Tolerance";
		}
	}

}
