package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;

public class SetParabolicTroughSemilatusRectumCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldSemilatusRectum;
	private double newSemilatusRectum;
	private final ParabolicTrough trough;

	public SetParabolicTroughSemilatusRectumCommand(final ParabolicTrough trough) {
		this.trough = trough;
		oldSemilatusRectum = trough.getApertureWidth();
	}

	public ParabolicTrough getParabolicTrough() {
		return trough;
	}

	public double getOldValue() {
		return oldSemilatusRectum;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newSemilatusRectum = trough.getSemilatusRectum();
		trough.setSemilatusRectum(oldSemilatusRectum);
		trough.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		trough.setSemilatusRectum(newSemilatusRectum);
		trough.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Semilatus Rectum for Selected Parabolic Trough";
	}

}
