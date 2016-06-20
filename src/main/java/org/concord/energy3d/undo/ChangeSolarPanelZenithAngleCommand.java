package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChangeSolarPanelZenithAngleCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private SolarPanel solarPanel;

	public ChangeSolarPanelZenithAngleCommand(SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.getZenithAngle();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = solarPanel.getZenithAngle();
		solarPanel.setZenithAngle(oldValue);
		solarPanel.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setZenithAngle(newValue);
		solarPanel.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Zenith Angle for Selected Solar Panel";
	}

}
