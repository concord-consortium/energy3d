package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class SetTrackerForAllSolarPanelsCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int[] oldValues, newValues;
	private List<SolarPanel> panels;

	public SetTrackerForAllSolarPanelsCommand() {
		panels = Scene.getInstance().getAllSolarPanels();
		int n = panels.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = panels.get(i).getTracker();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = panels.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			SolarPanel p = panels.get(i);
			newValues[i] = p.getTracker();
			p.setTracker(oldValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = panels.size();
		for (int i = 0; i < n; i++) {
			SolarPanel p = panels.get(i);
			p.setTracker(newValues[i]);
			p.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		switch (panels.get(0).getTracker()) {
		case SolarPanel.AZIMUTH_ALTITUDE_DUAL_AXIS_TRACKER:
			return "Enable Dual-Axis Tracker for All Solar Panels";
		case SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER:
			return "Enable Single-Axis Tracker for All Solar Panels";
		default:
			return "Disable Tracker for All Solar Panels";
		}
	}

}
