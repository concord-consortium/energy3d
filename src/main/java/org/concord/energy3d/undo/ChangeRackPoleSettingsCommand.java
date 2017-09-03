package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class ChangeRackPoleSettingsCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldDx;
	private double newDx;
	private final double oldDy;
	private double newDy;
	private final boolean oldVisible;
	private boolean newVisible;
	private final Rack rack;

	public ChangeRackPoleSettingsCommand(final Rack rack) {
		this.rack = rack;
		oldDx = rack.getPoleDistanceX();
		oldDy = rack.getPoleDistanceY();
		oldVisible = rack.isPoleVisible();
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

	public boolean isPreviouslyVisible() {
		return oldVisible;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newDx = rack.getPoleDistanceX();
		newDy = rack.getPoleDistanceY();
		newVisible = rack.isPoleVisible();
		rack.setPoleDistanceX(oldDx);
		rack.setPoleDistanceY(oldDy);
		rack.setPoleVisible(oldVisible);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.setPoleDistanceX(newDx);
		rack.setPoleDistanceY(newDy);
		rack.setPoleVisible(newVisible);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Pole Settings for Rack";
	}

}
