package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

public class ChangeLatitudeCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;

	public ChangeLatitudeCommand() {
		oldValue = Heliodon.getInstance().getLatitude();
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Heliodon.getInstance().getLatitude();
		Heliodon.getInstance().setLatitude(oldValue);
		Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), (int) Math.round(180 * oldValue / Math.PI));
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Heliodon.getInstance().setLatitude(newValue);
		Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), (int) Math.round(180 * newValue / Math.PI));
	}

	@Override
	public String getPresentationName() {
		return "Change Latitude";
	}

}
