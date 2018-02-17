package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PvModuleSpecs;

public class ChangeSolarPanelModelCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final PvModuleSpecs oldModel;
	private PvModuleSpecs newModel;
	private final SolarPanel solarPanel;

	public ChangeSolarPanelModelCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldModel = solarPanel.getPvModuleSpecs();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public PvModuleSpecs getOldModel() {
		return oldModel;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newModel = solarPanel.getPvModuleSpecs();
		solarPanel.setPvModuleSpecs(oldModel);
		solarPanel.draw();
		SceneManager.getInstance().refresh();
		EnergyPanel.getInstance().updateProperties();
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setPvModuleSpecs(newModel);
		solarPanel.draw();
		SceneManager.getInstance().refresh();
		EnergyPanel.getInstance().updateProperties();
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public String getPresentationName() {
		return "Model Change for Selected Solar Panel";
	}

}
