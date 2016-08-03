package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class SetSolarTrackerCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int oldValue, newValue;
	private SolarPanel sp;

	public SetSolarTrackerCommand(SolarPanel sp) {
		this.sp = sp;
		oldValue = sp.getTracker();
	}

	public SolarPanel getSolarPanel() {
		return sp;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = sp.getTracker();
		sp.setTracker(oldValue);
		sp.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		sp.setTracker(newValue);
		sp.draw();
	}

	@Override
	public String getPresentationName() {
		switch (oldValue) {
		case SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER:
			return "Enable Dual-Axis Tracker";
		case SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER:
			return "Enable Single-Axis Tracker";
		default:
			return "Disable Tracker";
		}
	}

}
