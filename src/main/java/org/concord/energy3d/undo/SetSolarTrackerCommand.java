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
	private final String presentationName;

	public SetSolarTrackerCommand(final Trackable tracker, final String presentationName) {
		this.tracker = tracker;
		oldValue = tracker.getTracker();
		this.presentationName = presentationName;
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
		return presentationName;
	}

}
