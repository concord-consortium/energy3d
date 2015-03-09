package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

public class ChangeLatitudeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgLatitude, newLatitude;

	public ChangeLatitudeCommand() {
		orgLatitude = Heliodon.getInstance().getLatitude();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newLatitude = Heliodon.getInstance().getLatitude();
		Heliodon.getInstance().setLatitude(orgLatitude);
		Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), (int) Math.round(180 * orgLatitude / Math.PI));
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Heliodon.getInstance().setLatitude(newLatitude);
		Util.setSilently(EnergyPanel.getInstance().getLatitudeSpinner(), (int) Math.round(180 * newLatitude / Math.PI));
	}

	@Override
	public String getPresentationName() {
		return "Change Latitude";
	}

}
