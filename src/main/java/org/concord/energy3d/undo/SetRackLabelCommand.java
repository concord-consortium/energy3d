package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;

public class SetRackLabelCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelCustom;
	private final boolean oldLabelCellEfficiency;
	private final boolean oldLabelTiltAngle;
	private final boolean oldLabelTracker;
	private final boolean oldLabelEnergyOutput;
	private boolean newLabelId;
	private boolean newLabelCustom;
	private boolean newLabelCellEfficiency;
	private boolean newLabelTiltAngle;
	private boolean newLabelTracker;
	private boolean newLabelEnergyOutput;
	private final Rack rack;

	public SetRackLabelCommand(final Rack rack) {
		this.rack = rack;
		oldLabelId = rack.getLabelId();
		oldLabelCustom = rack.getLabelCustom();
		oldLabelCellEfficiency = rack.getLabelCellEfficiency();
		oldLabelTiltAngle = rack.getLabelTiltAngle();
		oldLabelTracker = rack.getLabelTracker();
		oldLabelEnergyOutput = rack.getLabelEnergyOutput();
	}

	public Rack getRack() {
		return rack;
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
		newLabelId = rack.getLabelId();
		newLabelCustom = rack.getLabelCustom();
		newLabelCellEfficiency = rack.getLabelCellEfficiency();
		newLabelTiltAngle = rack.getLabelTiltAngle();
		newLabelTracker = rack.getLabelTracker();
		newLabelEnergyOutput = rack.getLabelEnergyOutput();
		rack.setLabelId(oldLabelId);
		rack.setLabelCustom(oldLabelCustom);
		rack.setLabelCellEfficiency(oldLabelCellEfficiency);
		rack.setLabelTiltAngle(oldLabelTiltAngle);
		rack.setLabelTracker(oldLabelTracker);
		rack.setLabelEnergyOutput(oldLabelEnergyOutput);
		rack.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		rack.setLabelId(newLabelId);
		rack.setLabelCustom(newLabelCustom);
		rack.setLabelCellEfficiency(newLabelCellEfficiency);
		rack.setLabelTiltAngle(newLabelTiltAngle);
		rack.setLabelTracker(newLabelTracker);
		rack.setLabelEnergyOutput(newLabelEnergyOutput);
		rack.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Rack";
	}

}
