package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;

public class ChangeBuildingSolarPanelTiltAngleCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] oldValues, newValues;
	private Foundation foundation;
	private List<SolarPanel> panels;

	public ChangeBuildingSolarPanelTiltAngleCommand(Foundation foundation) {
		this.foundation = foundation;
		panels = Scene.getInstance().getSolarPanelsOfBuilding(foundation);
		int n = panels.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getTiltAngle();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = panels.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = panels.get(i).getTiltAngle();
			panels.get(i).setTiltAngle(oldValues[i]);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = panels.size();
		for (int i = 0; i < n; i++) {
			panels.get(i).setTiltAngle(newValues[i]);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Change Tilt Angle for All Solar Panels of Selected Building";
	}

}
