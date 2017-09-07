package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Trackable;

public class SetSolarTrackerCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Trackable tracker;

	public SetSolarTrackerCommand(final Trackable tracker) {
		this.tracker = tracker;
		oldValue = tracker.getTracker();
	}

	public Trackable getTracker() {
		return tracker;
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = tracker.getTracker();
		tracker.setTracker(oldValue);
		if (tracker instanceof HousePart) {
			((HousePart) tracker).draw();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		tracker.setTracker(newValue);
		if (tracker instanceof HousePart) {
			((HousePart) tracker).draw();
		}
	}

	@Override
	public String getPresentationName() {
		switch (oldValue) {
		case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
			return "Enable Dual-Axis Tracker";
		case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
			return "Enable Horizontal Single-Axis Tracker";
		case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
			return "Enable Vertical Single-Axis Tracker";
		default:
			return "Disable Tracker";
		}
	}

}
