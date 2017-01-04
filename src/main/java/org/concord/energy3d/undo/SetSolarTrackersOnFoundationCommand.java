package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.scene.SceneManager;

public class SetSolarTrackersOnFoundationCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final Foundation foundation;
	private final List<SolarPanel> solarPanels;
	private final List<Rack> racks;
	private final Trackable tracker;

	public SetSolarTrackersOnFoundationCommand(final Foundation foundation, final Trackable tracker) {
		this.foundation = foundation;
		int n = 0;
		if (tracker instanceof SolarPanel) {
			solarPanels = foundation.getSolarPanels();
			n = solarPanels.size();
			oldValues = new int[n];
			for (int i = 0; i < n; i++) {
				oldValues[i] = solarPanels.get(i).getTracker();
			}
			racks = null;
		} else if (tracker instanceof Rack) {
			racks = foundation.getRacks();
			n = racks.size();
			oldValues = new int[n];
			for (int i = 0; i < n; i++) {
				oldValues[i] = racks.get(i).getTracker();
			}
			solarPanels = null;
		} else {
			solarPanels = null;
			racks = null;
			oldValues = null;
		}
		this.tracker = tracker;
	}

	public Trackable getTracker() {
		return tracker;
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (solarPanels != null) {
			final int n = solarPanels.size();
			newValues = new int[n];
			for (int i = 0; i < n; i++) {
				final SolarPanel s = solarPanels.get(i);
				newValues[i] = s.getTracker();
				s.setTracker(oldValues[i]);
				s.draw();
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
		if (solarPanels != null) {
			final int n = solarPanels.size();
			for (int i = 0; i < n; i++) {
				final SolarPanel s = solarPanels.get(i);
				s.setTracker(newValues[i]);
				s.draw();
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
		if (solarPanels != null) {
			switch (solarPanels.get(0).getTracker()) {
			case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
				return "Enable Dual-Axis Tracker for All Solar Panels on Selected Foundation";
			case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
				return "Enable Horizontal Single-Axis Tracker for All Solar Panels on Selected Foundation";
			case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
				return "Enable Vertical Single-Axis Tracker for All Solar Panels on Selected Foundation";
			default:
				return "Disable Tracker for All Solar Panels on Selected Foundation";
			}
		} else if (racks != null) {
			switch (racks.get(0).getTracker()) {
			case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
				return "Enable Dual-Axis Tracker for All Racks on Selected Foundation";
			case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
				return "Enable Horizontal Single-Axis Tracker for All Racks on Selected Foundation";
			case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
				return "Enable Vertical Single-Axis Tracker for All Racks on Selected Foundation";
			default:
				return "Disable Tracker for All Racks on Selected Foundation";
			}
		}
		return "Change Tracker on Selected Foundation";
	}

}
