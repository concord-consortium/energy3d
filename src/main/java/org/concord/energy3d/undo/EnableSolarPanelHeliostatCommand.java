package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class EnableSolarPanelHeliostatCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;
	private SolarPanel sp;

	public EnableSolarPanelHeliostatCommand(SolarPanel sp) {
		this.sp = sp;
		oldValue = sp.getHeliostat();
	}

	public SolarPanel getSolarPanel() {
		return sp;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = sp.getHeliostat();
		sp.setHeliostat(oldValue);
		sp.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		sp.setHeliostat(newValue);
		sp.draw();
	}

	@Override
	public String getPresentationName() {
		return oldValue ? "Disable Heliostat" : "Enable Heliostat";
	}

}
