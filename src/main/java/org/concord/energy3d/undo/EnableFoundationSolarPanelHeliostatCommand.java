package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class EnableFoundationSolarPanelHeliostatCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean[] oldValues, newValues;
	private Foundation foundation;
	private List<SolarPanel> solarPanels;

	public EnableFoundationSolarPanelHeliostatCommand(Foundation foundation) {
		this.foundation = foundation;
		solarPanels = Scene.getInstance().getSolarPanelsOfBuilding(foundation);
		int n = solarPanels.size();
		oldValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = solarPanels.get(i).getHeliostat();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = solarPanels.size();
		newValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			SolarPanel s = solarPanels.get(i);
			newValues[i] = s.getHeliostat();
			s.setHeliostat(oldValues[i]);
			s.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = solarPanels.size();
		for (int i = 0; i < n; i++) {
			SolarPanel s = solarPanels.get(i);
			s.setHeliostat(newValues[i]);
			s.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return (solarPanels.get(0).getHeliostat() ? "Enable" : "Disable") + " Heliostat for All Solar Panels on Selected Platform";
	}

}
