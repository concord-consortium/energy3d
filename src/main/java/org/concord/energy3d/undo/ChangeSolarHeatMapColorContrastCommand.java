package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

public class ChangeSolarHeatMapColorContrastCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int orgContrast, newContrast;

	public ChangeSolarHeatMapColorContrastCommand() {
		orgContrast = Scene.getInstance().getSolarHeatMapColorContrast();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newContrast = Scene.getInstance().getSolarHeatMapColorContrast();
		Scene.getInstance().setSolarHeatMapColorContrast(orgContrast);
		Util.setSilently(EnergyPanel.getInstance().getColorMapSlider(), orgContrast);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setSolarHeatMapColorContrast(newContrast);
		Util.setSilently(EnergyPanel.getInstance().getColorMapSlider(), newContrast);
	}

	@Override
	public String getPresentationName() {
		return "Change Solar Heat Map Color Contrast";
	}

}
