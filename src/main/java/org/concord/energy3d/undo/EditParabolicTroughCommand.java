package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;

import com.ardor3d.math.Vector3;

public class EditParabolicTroughCommand extends EditPartCommand {

	private static final long serialVersionUID = 1L;
	private final double oldTroughWidth, oldTroughLength;
	private final Vector3 oldTroughCenter;
	private double newTroughWidth, newTroughLength;
	private Vector3 newTroughCenter;

	public EditParabolicTroughCommand(final ParabolicTrough trough) {
		super(trough);
		oldTroughWidth = trough.getApertureWidth();
		oldTroughLength = trough.getTroughLength();
		oldTroughCenter = trough.getPoints().get(0).clone();
	}

	@Override
	public void undo() throws CannotUndoException {
		final ParabolicTrough trough = (ParabolicTrough) housePart;
		newTroughCenter = trough.getPoints().get(0).clone();
		newTroughWidth = trough.getApertureWidth();
		newTroughLength = trough.getTroughLength();
		trough.getPoints().get(0).set(oldTroughCenter);
		trough.setApertureWidth(oldTroughWidth);
		trough.setTroughLength(oldTroughLength);
		super.undo();
	}

	@Override
	public void redo() throws CannotRedoException {
		final ParabolicTrough trough = (ParabolicTrough) housePart;
		trough.getPoints().get(0).set(newTroughCenter);
		trough.setApertureWidth(newTroughWidth);
		trough.setTroughLength(newTroughLength);
		super.redo();
	}

	@Override
	public String getPresentationName() {
		return "Edit Parabolic Trough";
	}

}
