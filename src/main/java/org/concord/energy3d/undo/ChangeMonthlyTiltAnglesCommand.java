package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class ChangeMonthlyTiltAnglesCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Rack rack;

	public ChangeMonthlyTiltAnglesCommand(final Rack rack) {
		this.rack = rack;
		oldValues = rack.getMonthlyTiltAngles();
	}

	public Rack getRack() {
		return rack;
	}

	public double[] getOldValues() {
		return oldValues;
	}

	public double[] getNewValues() {
		return newValues;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValues = rack.getMonthlyTiltAngles();
		rack.setMonthlyTiltAngles(oldValues);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.setMonthlyTiltAngles(newValues);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Monthly Tilt Angles";
	}

}
