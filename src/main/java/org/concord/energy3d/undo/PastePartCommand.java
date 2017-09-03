package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class PastePartCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final HousePart housePart;

	public PastePartCommand(final HousePart housePart) {
		this.housePart = housePart;
	}

	public HousePart getPart() {
		return housePart;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().remove(housePart, true);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().add(housePart, true);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Paste " + housePart.getClass().getSimpleName();
	}

}
