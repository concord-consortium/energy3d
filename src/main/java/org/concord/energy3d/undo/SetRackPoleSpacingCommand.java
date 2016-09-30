package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class SetRackPoleSpacingCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldDx;
	private double newDx;
	private final double oldDy;
	private double newDy;
	private final Rack rack;

	public SetRackPoleSpacingCommand(final Rack rack) {
		this.rack = rack;
		oldDx = rack.getPoleDistanceX();
		oldDy = rack.getPoleDistanceY();
	}

	public Rack getRack() {
		return rack;
	}

	public double getOldPoleDistanceX() {
		return oldDx;
	}

	public double getOldPoleDistanceY() {
		return oldDy;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newDx = rack.getPoleDistanceX();
		newDy = rack.getPoleDistanceY();
		rack.setPoleDistanceX(oldDx);
		rack.setPoleDistanceY(oldDy);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.setPoleDistanceX(newDx);
		rack.setPoleDistanceY(newDy);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Pole Distance for Rack";
	}

}
