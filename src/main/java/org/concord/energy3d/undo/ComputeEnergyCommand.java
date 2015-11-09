package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

public class ComputeEnergyCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private boolean oldValue, newValue;

	public ComputeEnergyCommand() {
		oldValue = SceneManager.getInstance().getSolarHeatMap();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = SceneManager.getInstance().getSolarHeatMap();
		SceneManager.getInstance().computeEnergyView(oldValue);
		Util.selectSilently(MainPanel.getInstance().getEnergyViewButton(), oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		SceneManager.getInstance().computeEnergyView(newValue);
		Util.selectSilently(MainPanel.getInstance().getEnergyViewButton(), newValue);
	}

	@Override
	public String getPresentationName() {
		return "Compute Energy";
	}

}
