package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;

public class SetFoundationSolarTrackerCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int[] oldValues, newValues;
	private Foundation foundation;
	private List<SolarPanel> solarPanels;

	public SetFoundationSolarTrackerCommand(Foundation foundation) {
		this.foundation = foundation;
		solarPanels = foundation.getSolarPanels();
		int n = solarPanels.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = solarPanels.get(i).getTracker();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = solarPanels.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			SolarPanel s = solarPanels.get(i);
			newValues[i] = s.getTracker();
			s.setTracker(oldValues[i]);
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
			s.setTracker(newValues[i]);
			s.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		switch (solarPanels.get(0).getTracker()) {
		case SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER:
			return "Enable Dual-Axis Tracker for All Solar Panels on Selected Foundation";
		case SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER:
			return "Enable Single-Axis Tracker for All Solar Panels on Selected Foundation";
		default:
			return "Disable Tracker for All Solar Panels on Selected Foundation";
		}
	}

}
