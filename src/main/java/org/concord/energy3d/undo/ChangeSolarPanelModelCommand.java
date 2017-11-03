package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PvModuleSpecs;
import org.concord.energy3d.simulation.PvModulesData;

public class ChangeSolarPanelModelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final String oldValue;
	private String newValue;
	private final SolarPanel solarPanel;

	public ChangeSolarPanelModelCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.getModelName();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public String getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = solarPanel.getModelName();
		final PvModuleSpecs specs = PvModulesData.getInstance().getModuleSpecs(oldValue);
		solarPanel.setPvModuleSpecs(specs);
		solarPanel.draw();
		SceneManager.getInstance().refresh();
		EnergyPanel.getInstance().updateProperties();
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final PvModuleSpecs specs = PvModulesData.getInstance().getModuleSpecs(newValue);
		solarPanel.setPvModuleSpecs(specs);
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
