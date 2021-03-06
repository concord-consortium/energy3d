package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class SetSolarPanelLabelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelModel;
	private final boolean oldLabelCustom;
	private final boolean oldLabelCellEfficiency;
	private final boolean oldLabelTiltAngle;
	private final boolean oldLabelTracker;
	private final boolean oldLabelEnergyOutput;
	private boolean newLabelId;
	private boolean newLabelModel;
	private boolean newLabelCustom;
	private boolean newLabelCellEfficiency;
	private boolean newLabelTiltAngle;
	private boolean newLabelTracker;
	private boolean newLabelEnergyOutput;
	private final SolarPanel panel;

	public SetSolarPanelLabelCommand(final SolarPanel panel) {
		this.panel = panel;
		oldLabelId = panel.getLabelId();
		oldLabelModel = panel.getLabelModelName();
		oldLabelCustom = panel.getLabelCustom();
		oldLabelCellEfficiency = panel.getLabelCellEfficiency();
		oldLabelTiltAngle = panel.getLabelTiltAngle();
		oldLabelTracker = panel.getLabelTracker();
		oldLabelEnergyOutput = panel.getLabelEnergyOutput();
	}

	public SolarPanel getSolarPanel() {
		return panel;
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
		newLabelId = panel.getLabelId();
		newLabelModel = panel.getLabelModelName();
		newLabelCustom = panel.getLabelCustom();
		newLabelCellEfficiency = panel.getLabelCellEfficiency();
		newLabelTiltAngle = panel.getLabelTiltAngle();
		newLabelTracker = panel.getLabelTracker();
		newLabelEnergyOutput = panel.getLabelEnergyOutput();
		panel.setLabelId(oldLabelId);
		panel.setLabelModelName(oldLabelModel);
		panel.setLabelCustom(oldLabelCustom);
		panel.setLabelCellEfficiency(oldLabelCellEfficiency);
		panel.setLabelTiltAngle(oldLabelTiltAngle);
		panel.setLabelTracker(oldLabelTracker);
		panel.setLabelEnergyOutput(oldLabelEnergyOutput);
		panel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		panel.setLabelId(newLabelId);
		panel.setLabelModelName(newLabelModel);
		panel.setLabelCustom(newLabelCustom);
		panel.setLabelCellEfficiency(newLabelCellEfficiency);
		panel.setLabelTiltAngle(newLabelTiltAngle);
		panel.setLabelTracker(newLabelTracker);
		panel.setLabelEnergyOutput(newLabelEnergyOutput);
		panel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Solar Panel";
	}

}
