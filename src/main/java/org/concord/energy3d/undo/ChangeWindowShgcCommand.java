package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Window;

public class ChangeWindowShgcCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgShgc, newShgc;
	private Window selectedWindow;

	public ChangeWindowShgcCommand(Window selectedWindow) {
		this.selectedWindow = selectedWindow;
		orgShgc = selectedWindow.getSolarHeatGainCoefficient();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newShgc = selectedWindow.getSolarHeatGainCoefficient();
		selectedWindow.setSolarHeatGainCoefficient(orgShgc);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		selectedWindow.setSolarHeatGainCoefficient(newShgc);
	}

	// for action logging
	public Window getWindow() {
		return selectedWindow;
	}

	@Override
	public String getPresentationName() {
		return "SHGC Change for Selected Window";
	}

}
