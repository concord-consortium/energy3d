package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PvModuleSpecs;

public class ChangeSolarPanelModelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final PvModuleSpecs oldSpecs;
	private PvModuleSpecs newSpecs;
	private final SolarPanel solarPanel;

	public ChangeSolarPanelModelCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldSpecs = solarPanel.getPvModuleSpecs();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public PvModuleSpecs getOldValue() {
		return oldSpecs;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newSpecs = solarPanel.getPvModuleSpecs();
		solarPanel.setPvModuleSpecs(oldSpecs);
		solarPanel.draw();
		SceneManager.getInstance().refresh();
		EnergyPanel.getInstance().updateProperties();
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setPvModuleSpecs(newSpecs);
		solarPanel.draw();
		SceneManager.getInstance().refresh();
		EnergyPanel.getInstance().updateProperties();
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		return "Model Change for Selected Solar Panel";
	}

}
