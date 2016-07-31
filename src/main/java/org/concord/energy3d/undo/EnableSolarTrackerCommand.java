package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class EnableSolarTrackerCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private SolarPanel sp;

	public EnableSolarTrackerCommand(SolarPanel sp) {
		this.sp = sp;
		oldValue = sp.isTrackerEnabled();
	}

	public SolarPanel getSolarPanel() {
		return sp;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = sp.isTrackerEnabled();
		sp.setTrackerEnabled(oldValue);
		sp.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		sp.setTrackerEnabled(newValue);
		sp.draw();
	}

	@Override
	public String getPresentationName() {
		return oldValue ? "Disable Solar Tracker" : "Enable Solar Tracker";
	}

}
