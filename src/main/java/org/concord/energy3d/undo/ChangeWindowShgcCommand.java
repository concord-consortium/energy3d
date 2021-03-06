package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Window;

public class ChangeWindowShgcCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final Window window;

	public ChangeWindowShgcCommand(final Window selectedWindow) {
		this.window = selectedWindow;
		oldValue = selectedWindow.getSolarHeatGainCoefficient();
	}

	public Window getWindow() {
		return window;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = window.getSolarHeatGainCoefficient();
		window.setSolarHeatGainCoefficient(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		window.setSolarHeatGainCoefficient(newValue);
	}

	@Override
	public String getPresentationName() {
		return "SHGC Change for Selected Window";
	}

}
