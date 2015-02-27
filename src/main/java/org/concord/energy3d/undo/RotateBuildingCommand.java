package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.PropertiesPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;

@SuppressWarnings("serial")
public class RotateBuildingCommand extends AbstractUndoableEdit {

	private final Foundation foundation;
	private final double rotationAngle;

	public RotateBuildingCommand(final Foundation foundation, double rotationAngle) {
		this.foundation = foundation;
		this.rotationAngle = rotationAngle;
	}

	// for action logging
	public HousePart getHFoundation() {
		return foundation;
	}

	public double getRotationAngle() {
		return rotationAngle;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		SceneManager.getInstance().undoOrRedoBuildingRotation(foundation, rotationAngle, true);
		PropertiesPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().undoOrRedoBuildingRotation(foundation, rotationAngle, false);
		PropertiesPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Rotate Building";
	}

}
