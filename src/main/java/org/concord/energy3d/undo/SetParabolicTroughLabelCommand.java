package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicTrough;

public class SetParabolicTroughLabelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelCustom;
	private final boolean oldLabelEnergyOutput;
	private boolean newLabelId;
	private boolean newLabelCustom;
	private boolean newLabelEnergyOutput;
	private final ParabolicTrough trough;

	public SetParabolicTroughLabelCommand(final ParabolicTrough trough) {
		this.trough = trough;
		oldLabelId = trough.getLabelId();
		oldLabelCustom = trough.getLabelCustom();
		oldLabelEnergyOutput = trough.getLabelEnergyOutput();
	}

	public ParabolicTrough getParabolicTrough() {
		return trough;
	}

	public boolean getOldLabelId() {
		return oldLabelId;
	}

	public boolean getNewLabelId() {
		return newLabelId;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newLabelId = trough.getLabelId();
		newLabelCustom = trough.getLabelCustom();
		newLabelEnergyOutput = trough.getLabelEnergyOutput();
		trough.setLabelId(oldLabelId);
		trough.setLabelCustom(oldLabelCustom);
		trough.setLabelEnergyOutput(oldLabelEnergyOutput);
		trough.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		trough.setLabelId(newLabelId);
		trough.setLabelCustom(newLabelCustom);
		trough.setLabelEnergyOutput(newLabelEnergyOutput);
		trough.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Parabolic Trough";
	}

}
