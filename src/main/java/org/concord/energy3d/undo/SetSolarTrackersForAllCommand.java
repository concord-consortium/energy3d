package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class SetSolarTrackersForAllCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final List<SolarPanel> panels;
	private List<Rack> racks;
	private final Trackable tracker;

	public SetSolarTrackersForAllCommand(final Trackable tracker) {
		if (tracker instanceof SolarPanel) {
			panels = Scene.getInstance().getAllSolarPanels();
			final int n = panels.size();
			oldValues = new int[n];
			for (int i = 0; i < n; i++) {
				oldValues[i] = panels.get(i).getTracker();
			}
			racks = null;
		} else if (tracker instanceof Rack) {
			racks = Scene.getInstance().getAllRacks();
			final int n = racks.size();
			oldValues = new int[n];
			for (int i = 0; i < n; i++) {
				oldValues[i] = racks.get(i).getTracker();
			}
			panels = null;
		} else {
			panels = null;
			racks = null;
			oldValues = null;
		}
		this.tracker = tracker;
	}

	public Trackable getTracker() {
		return tracker;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (panels != null) {
			final int n = panels.size();
			newValues = new int[n];
			for (int i = 0; i < n; i++) {
				final SolarPanel p = panels.get(i);
				newValues[i] = p.getTracker();
				p.setTracker(oldValues[i]);
				p.draw();
			}
		} else if (racks != null) {
			final int n = racks.size();
			newValues = new int[n];
			for (int i = 0; i < n; i++) {
				final Rack r = racks.get(i);
				newValues[i] = r.getTracker();
				r.setTracker(oldValues[i]);
				r.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (panels != null) {
			final int n = panels.size();
			for (int i = 0; i < n; i++) {
				final SolarPanel p = panels.get(i);
				p.setTracker(newValues[i]);
				p.draw();
			}
		} else if (racks != null) {
			final int n = racks.size();
			for (int i = 0; i < n; i++) {
				final Rack r = racks.get(i);
				r.setTracker(newValues[i]);
				r.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		if (panels != null) {
			switch (panels.get(0).getTracker()) {
			case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
				return "Enable Dual-Axis Tracker for All Solar Panels";
			case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
				return "Enable Horizontal Single-Axis Tracker for All Solar Panels";
			case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
				return "Enable Vertical Single-Axis Tracker for All Solar Panels";
			default:
				return "Disable Tracker for All Solar Panels";
			}
		} else if (racks != null) {
			switch (racks.get(0).getTracker()) {
			case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
				return "Enable Dual-Axis Tracker for All Racks";
			case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
				return "Enable Horizontal Single-Axis Tracker for All Racks";
			case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
				return "Enable Vertical Single-Axis Tracker for All Racks";
			default:
				return "Disable Tracker for All Racks";
			}
		} else {
			return "Change Trackers";
		}
	}

}
