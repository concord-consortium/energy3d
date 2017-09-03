package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class SetTemperatureEffectsCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldPmax;
	private double newPmax;
	private final double oldNoct;
	private double newNoct;
	private final SolarPanel solarPanel;

	public SetTemperatureEffectsCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldPmax = solarPanel.getTemperatureCoefficientPmax();
		oldNoct = solarPanel.getNominalOperatingCellTemperature();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public double getOldPmax() {
		return oldPmax;
	}

	public double getOldNoct() {
		return oldNoct;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newPmax = solarPanel.getTemperatureCoefficientPmax();
		newNoct = solarPanel.getNominalOperatingCellTemperature();
		solarPanel.setTemperatureCoefficientPmax(oldPmax);
		solarPanel.setNominalOperatingCellTemperature(oldNoct);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setTemperatureCoefficientPmax(newPmax);
		solarPanel.setNominalOperatingCellTemperature(newNoct);
	}

	@Override
	public String getPresentationName() {
		return "Temperature Effects Change for Selected Solar Panel";
	}

}
