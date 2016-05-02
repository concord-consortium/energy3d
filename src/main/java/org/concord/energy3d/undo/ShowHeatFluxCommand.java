package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

public class ShowHeatFluxCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;

	public ShowHeatFluxCommand() {
		oldValue = Scene.getInstance().getAlwaysComputeHeatFluxVectors();
		newValue = !oldValue;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().setAlwaysComputeHeatFluxVectors(oldValue);
		Util.selectSilently(MainFrame.getInstance().getHeatFluxMenuItem(), oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setAlwaysComputeHeatFluxVectors(newValue);
		Util.selectSilently(MainFrame.getInstance().getHeatFluxMenuItem(), newValue);
	}

	@Override
	public String getPresentationName() {
		return "Show Heat Flux Vectors";
	}

}
