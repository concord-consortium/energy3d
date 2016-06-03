package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;

public class ChangeBuildingMicroInverterEfficiencyCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] oldValues, newValues;
	private Foundation foundation;
	private List<SolarPanel> panels;

	public ChangeBuildingMicroInverterEfficiencyCommand(Foundation foundation) {
		this.foundation = foundation;
		panels = Scene.getInstance().getSolarPanelsOfBuilding(foundation);
		int n = panels.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getInverterEfficiency();
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
			newValues[i] = panels.get(i).getInverterEfficiency();
			panels.get(i).setInverterEfficiency(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = panels.size();
		for (int i = 0; i < n; i++) {
			panels.get(i).setInverterEfficiency(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Micro Inverter Efficiency Change for All Solar Panels of Selected Building";
	}

}