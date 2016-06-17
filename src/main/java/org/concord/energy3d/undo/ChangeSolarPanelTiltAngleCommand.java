package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;

public class ChangeSolarPanelTiltAngleCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private SolarPanel solarPanel;

	public ChangeSolarPanelTiltAngleCommand(SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.getTiltAngle();
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
		newValue = solarPanel.getTiltAngle();
		solarPanel.setTiltAngle(oldValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setTiltAngle(newValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Change Tilt Angle for Selected Solar Panel";
	}

}
