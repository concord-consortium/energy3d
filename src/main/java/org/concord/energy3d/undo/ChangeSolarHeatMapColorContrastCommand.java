package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

public class ChangeSolarHeatMapColorContrastCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int oldValue, newValue;

	public ChangeSolarHeatMapColorContrastCommand() {
		oldValue = Scene.getInstance().getSolarHeatMapColorContrast();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getSolarHeatMapColorContrast();
		Scene.getInstance().setSolarHeatMapColorContrast(oldValue);
		Util.setSilently(EnergyPanel.getInstance().getColorMapSlider(), oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setSolarHeatMapColorContrast(newValue);
		Util.setSilently(EnergyPanel.getInstance().getColorMapSlider(), newValue);
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public String getPresentationName() {
		return "Change Solar Heat Map Color Contrast";
	}

}
