package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.SolarPanel;

public class ChangeTemperatureCoefficientPmaxCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final SolarPanel solarPanel;

	public ChangeTemperatureCoefficientPmaxCommand(final SolarPanel solarPanel) {
		this.solarPanel = solarPanel;
		oldValue = solarPanel.getTemperatureCoefficientPmax();
	}

	public SolarPanel getSolarPanel() {
		return solarPanel;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = solarPanel.getTemperatureCoefficientPmax();
		solarPanel.setTemperatureCoefficientPmax(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		solarPanel.setTemperatureCoefficientPmax(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Temperature Coefficient of Pmax Change for Selected Solar Panel";
	}

}
