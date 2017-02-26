package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

import com.ardor3d.math.Vector3;

public class EditRackCommand extends EditPartCommand {
	private static final long serialVersionUID = 1L;
	private final double oldRackWidth, oldRackHeight;
	private final Vector3 oldRackCenter;
	private double newRackWidth, newRackHeight;
	private Vector3 newRackCenter;

	public EditRackCommand(final Rack rack) {
		super(rack);
		oldRackWidth = rack.getRackWidth();
		oldRackHeight = rack.getRackHeight();
		oldRackCenter = rack.getPoints().get(0).clone();
	}

	@Override
	public void undo() throws CannotUndoException {
		final Rack rack = (Rack) housePart;
		newRackCenter = rack.getPoints().get(0).clone();
		newRackWidth = rack.getRackWidth();
		newRackHeight = rack.getRackHeight();
		rack.getPoints().get(0).set(oldRackCenter);
		rack.setRackWidth(oldRackWidth);
		rack.setRackHeight(oldRackHeight);
		super.undo();
	}

	@Override
	public void redo() throws CannotRedoException {
		final Rack rack = (Rack) housePart;
		rack.getPoints().get(0).set(newRackCenter);
		rack.setRackWidth(newRackWidth);
		rack.setRackHeight(newRackHeight);
		super.redo();
	}

	@Override
	public String getPresentationName() {
		return "Edit Rack";
	}
}
