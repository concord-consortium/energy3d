package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

public class ChangeInsideTemperatureCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int orgTemperature, newTemperature;

	public ChangeInsideTemperatureCommand() {
		orgTemperature = Scene.getInstance().getInsideTemperature();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newTemperature = Scene.getInstance().getInsideTemperature();
		Scene.getInstance().setInsideTemperature(orgTemperature);
		Util.setSilently(EnergyPanel.getInstance().getInsideTemperatureSpinner(), orgTemperature);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setInsideTemperature(newTemperature);
		Util.setSilently(EnergyPanel.getInstance().getInsideTemperatureSpinner(), newTemperature);
	}

	@Override
	public String getPresentationName() {
		return "Change Inside Temperature";
	}

}
